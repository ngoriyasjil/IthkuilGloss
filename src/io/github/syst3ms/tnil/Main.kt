package io.github.syst3ms.tnil

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.MarkdownUtil
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("tnilgloss")!!

val authorizedUsers = arrayListOf<String>()

fun main() {
    val tokenFile = File("./token.txt")
    require((tokenFile.exists() && tokenFile.isFile)) { "Can't find token file!" }
    val lines = arrayListOf<String>()
    tokenFile.bufferedReader()
        .useLines { it.forEach { e -> lines.add(e) } }
    authorizedUsers += lines.drop(1)
    val jda = JDABuilder.createDefault(lines[0])
        .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "?help for info"))
        .addEventListeners(MessageListener())
        .build()
    jda.awaitReady()
}

class MessageListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channelType != ChannelType.TEXT && event.channelType != ChannelType.PRIVATE)
            return
        val chan = event.channel
        val msg = event.message
        val content = msg.contentRaw
        if (!content.startsWith("?")) {
            return
        }
        if (event.channelType == ChannelType.TEXT && !event.textChannel.canTalk(event.guild.selfMember)) {
            println("Can't talk in channel #" + chan.name)
            return
        }
        var first = content.split("\\s+".toRegex())[0]
        val ignoreDefault = if (first.startsWith("??")) {
            first = first.drop(2)
            false
        } else {
            first = first.drop(1)
            true
        }
        when (first) {
            "help" -> {
                val helpMessage = File("./src/main/java/io/github/syst3ms/tnil/help.md").readText().split("splitmessagehere")
                val newMessage = MessageBuilder()
                        .append(helpMessage[0])
                val second = MessageBuilder()
                        .append(helpMessage[1])
                val auth = event.author
                if (event.channelType == ChannelType.TEXT) {
                    auth.openPrivateChannel()
                        .flatMap { it.sendMessage(newMessage.build()) }
                        .flatMap { it.channel.sendMessage(second.build()) }
                        .queue({
                            chan.sendMessage("Help was sent your way, " + auth.asMention + "!").queue()
                        }) { // Failure
                            val m = second.append("\n")
                                .append("(Couldn't send the message in DMs, ")
                                .append(auth.asMention)
                                .append(")")
                                .build()
                            chan.sendMessage(newMessage.build())
                                .queue()
                            chan.sendMessage(m)
                                .queue()
                        }
                } else {
                    chan.sendMessage(newMessage.build())
                        .queue()
                }
            }
            "gloss", "short", "full", "!debug" -> { // Word-by-word parsing, precision 1
                val prec = if (first.contains("short", ignoreCase = true)) {
                    0
                } else if (first.contains("full")) {
                    2
                } else if (event.author.id in authorizedUsers && first.contains("debug")) {
                    3
                } else {
                    1
                }
                val parts = content.split("[\\s.;,:]+".toRegex()).filter(String::isNotBlank)
                val glosses = arrayListOf<String>()
                for (i in 1 until parts.size) {
                    var w = parts[i].toLowerCase().replace("’", "'")
                    if (w.startsWith("_") || w.startsWith("/")) {
                        w = w.substring(1)
                    } else {
                      val nonIthkuil = w.filter {
                        it.toString().defaultForm() !in CONSONANTS &&
                        VOWEL_FORM.none { v -> v eq it.toString() } }
                      if(nonIthkuil.isNotEmpty()) {
                        glosses += error("Non-ithkuil characters detected: " +
                          nonIthkuil.map { "\"$it\" (" + it.toInt().toString(16) + ")" }.joinToString())
                        continue
                      }
                    }
                    val res = try {
                        parseWord(w, prec, ignoreDefault)
                    } catch (ex: Exception) {
                        logger.error("{}", ex)
                        if (prec < 3) {
                            error("A severe exception occurred during sentence parsing. We are unable to give more information. " +
                                    "For a more thorough (but technical) description of the error, please use debug mode.")
                        } else {
                            val sw = StringWriter()
                            ex.printStackTrace(PrintWriter(sw))
                            val stacktrace = sw.toString()
                                    .split("\n")
                                    .take(10)
                                    .joinToString("\n")
                            error(stacktrace)
                        }
                    }
                    glosses += res.trim()
                }
                val newMessage = glosses.mapIndexed { i, s ->
                    MarkdownUtil.bold(parts[i+1] + ":") + " " + if (s.startsWith("\u0000")) {
                        MarkdownUtil.italics(s.substring(1, s.length))
                    } else {
                        s
                    }
                }.joinToString("\n", MarkdownUtil.underline("Gloss: ") + "\n")
                chan.sendMessage(newMessage.withZWS())
                    .queue()
            }
            "s", "sgloss", "sshort", "sfull", "!sdebug" -> { // Full sentence
                val prec = if (first.contains("short", ignoreCase = true)) {
                    0
                } else if (first.contains("full")) {
                    2
                } else if (event.author.id in authorizedUsers && first.contains("debug")) {
                    3
                } else {
                    1
                }
                val sentences = content.split("\\s*\\.\\s*".toRegex())
                        .asSequence()
                        .filter(String::isNotBlank)
                        .mapIndexed { i, s ->
                            if (i == 0) {
                                s.drop(first.length + 2)
                            } else {
                                s
                            }
                        }
                        .map { parseSentence(it.replace("’", "'"), prec, ignoreDefault) }
                        .map {
                            if (it[0] == "\u0000") {
                                it[0] + it[1]
                            } else {
                                it.joinToString("    ")
                            }
                        }
                        .reduce { acc, s -> when {
                                acc.startsWith("\u0000") -> acc
                                s.startsWith("\u0000") -> s
                                else -> "$acc  //  $s"
                            }
                        }
                val newMessage = MarkdownUtil.underline("Gloss:") + " " + if (sentences.startsWith("\u0000")) {
                    MarkdownUtil.italics(sentences.drop(1))
                } else {
                    sentences
                }
                chan.sendMessage(newMessage.withZWS())
                        .queue()
            }
            "!stop" -> {
                if (event.author.id in authorizedUsers) {
                    exitProcess(0)
                }
            }
            "!reloadexternal" -> {
                if (event.author.id in authorizedUsers) {
                    affixData = loadAffixes()
                    rootData = loadRoots()
                    event.textChannel.sendMessage("External resources successfully reloaded !")
                                     .queue()
                }
            }
        }
    }
}
