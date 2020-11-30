@file:OptIn(ExperimentalTime::class)

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
import org.slf4j.LoggerFactory
import java.net.URL
import kotlin.time.*

val logger = LoggerFactory.getLogger("tnilgloss")!!

val leStart = System.currentTimeMillis()

const val MORPHOPHONOLOGY_VERSION = "0.17.2"

fun main() {
    val tokenFile = File("./resources/token.txt")
    require(tokenFile.exists() && tokenFile.isFile) { "Can't find token file!" }
    val token = tokenFile.readLines()[0]
    loadResources()
    val jda = JDABuilder.createDefault(token)
        .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "?help for info"))
        .addEventListeners(MessageListener())
        .build()
    jda.awaitReady()
}

fun parsePrecision(request: String) =
        when {
            request.contains("short") -> 0
            request.contains("full")  -> 2
            request.contains("debug") -> 3
            else                      -> 1
        }

fun loadResources() {
  affixData = parseAffixes(URL("https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516").readText())
  rootData = parseRoots(URL("https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303").readText())
}

fun respond(content: String) : String? {
    var request = content.split("\\s+".toRegex())[0]
    val ignoreDefault = !request.startsWith("??")
    request = request.removePrefix("??").removePrefix("?")
    val prec = parsePrecision(request)

    when(request) {
        "gloss", "short", "full", "!debug" -> { // Word by word
            val words = content.split("[\\s.;,:]+".toRegex()).filter(kotlin.String::isNotBlank).drop(1)
            val glosses = arrayListOf<String>()
            for (word in words) {
                var w = word.toLowerCase().replace("’", "'").replace("\u200b", "")
                if (w.startsWith("_") || w.startsWith("/")) {
                    w = w.substring(1)
                } else {

                    val nonIthkuil = w.filter {
                        it.toString().defaultForm() !in ITHKUIL_CHARS }
                    if(nonIthkuil.isNotEmpty()) {
                        glosses += error("Non-ithkuil characters detected: " +
                                nonIthkuil.map { "\"$it\" (" + it.toInt().toString(16) + ")" }.joinToString() +
                                if (nonIthkuil.contains("[qˇ^ʰ]".toRegex())) " You might be writing in Ithkuil III. Try \"!gloss\" instead." else "")
                        continue
                    }
                }
                val res = try {
                    parseWord(w, prec, ignoreDefault)
                } catch (ex: Exception) {
                    logger.error("{}", ex)
                    if (prec < 3) {
                        error("A severe exception occurred during sentence parsing. Please contact the maintainers.")

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
                MarkdownUtil.bold(words[i] + ":") + " " + if (s.startsWith("\u0000")) {
                    MarkdownUtil.italics(s.substring(1, s.length))
                } else {
                    s
                }
            }.joinToString("\n", MarkdownUtil.underline("Gloss: ") + "\n")

            return newMessage.withZeroWidthSpaces()
        }
        "s", "sgloss", "sshort", "sfull", "!sdebug" -> { // Full sentence
            val sentences = content.split("\\s*\\.\\s*".toRegex())
                    .asSequence()
                    .filter(kotlin.String::isNotBlank)
                    .mapIndexed { i, s ->
                        if (i == 0) {
                            s.drop(request.length + 2)
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
            return newMessage.withZeroWidthSpaces()
        }
        "!stop" -> exitProcess(0)
        "!reload" -> {
            return try {
                loadResources()
                "External resources successfully reloaded!"
            } catch(e: Exception) {
                logger.error("{}", e)
                "Error while reloading external resources…"
            }
        }
        "!status" -> {
            val git = ProcessBuilder("git", "log", "-1", "--oneline").start()
            val lastCommit = String(git.getInputStream().readBytes())
            return listOf(
                  "__Status report:__",
                  "**Ithkuil Version:** $MORPHOPHONOLOGY_VERSION",
                  "**Roots:** ${rootData.size}",
                  "**Affixes:** ${affixData.size}", 
                  "**Help file exists:** ${File("./resources/help.md").exists()}",
                  "**Uptime:** ${(System.currentTimeMillis() - leStart).milliseconds}",
                  "**Last commit:** ${lastCommit}"
              ).joinToString("\n")
        }

        "!whosagoodbot", "!whosacutebot" -> return "(=^ェ^=✿)"
        else -> return null
    }
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

        if (content.startsWith("?help")) {
            val helpMessage = File("./resources/help.md").readText().split("SPLITMESSAGEHERE")
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
                                    .append("(Couldn't send the message in DMs, ${auth.asMention})")
                                    .build()
                            chan.sendMessage(newMessage.build())
                                    .queue()
                            chan.sendMessage(m)
                                    .queue()
                        }
            } else {
                chan.sendMessage(newMessage.build())
                        .queue()
                return
            }
        }

        val response = respond(content)
        if (response != null) {
            chan.sendMessage(MessageBuilder(response).build()).queue()
        }
    }
}
