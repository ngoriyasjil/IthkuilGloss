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

val authorizedUsers = arrayListOf<String>()

fun main() {
    val tokenFile = File("./token")
    require((tokenFile.exists() && tokenFile.isFile)) { "Can't find token file !" }
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
                val newMessage = MessageBuilder()
                        .append("Prefixes: \n")
                        .append("  - `?` : commands used with this prefix won't write the default values of some grammatical categories.\n")
                        .append("  - `??` : commands used with this prefix will write the values of all morphological categories even when default.\n")
                        .append("\n")
                        .append("General commands (must be preceded by the proper prefix) :\n")
                        .append("  - `gloss` : gives a morphological analysis of each subsequent word, with default precision\n")
                        .append("  - `full` : gives a morphological analysis of each subsequent word, with strong precision\n")
                        .append("  - `short` : gives a morphological analysis of each subsequent word, with weak precision\n")
                        .append("  - `s` or `sgloss` : gives a morphological analysis of the whole following sentence, with default precision\n")
                        .append("  - `sfull` : gives a morphological analysis of the whole following sentence, with strong precision\n")
                        .append("  - `sshort` : gives a morphological analysis of the whole following sentence, with weak precision\n")
                        .append("\n")
                        .append("Precision:\n")
                        .append("  - Default precision : all morphological components except affixes are abbreviated, roots may change depending on the stem\n")
                        .append("  - Strong precision : all morphological components are completely written out, roots may change depending on the stem\n")
                        .append("  - Weak precision : all morphological components are abbreviated, roots will only display their generic title\n")
                        .append("\n")
                        .append("The parsing logic is far from perfect (and also difficult to improve substantially), so if an error message looks like nonsense to you,\n")
                        .append("it's probably actual nonsense caused by the algorithm not interpreting the structure of your input properly. If however the error pertains to\n")
                        .append("the actual type of word you are trying to parse, there may be an actual bug, to which case make sure to let me (Syst3ms#9959) know.")
                val auth = event.author
                if (event.channelType == ChannelType.TEXT) {
                    auth.openPrivateChannel()
                        .flatMap { it.sendMessage(newMessage.build()) }
                        .queue({
                            chan.sendMessage("Help was sent your way, " + auth.asMention + "!").queue()
                        }) { // Failure
                            val m = newMessage.append("\n")
                                .append("(Couldn't send the message in DMs, ")
                                .append(auth.asMention)
                                .append(")")
                                .build()
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
                val parts = content.split("[\\s.;,:]+".toRegex())
                val glosses = arrayListOf<String>()
                for (i in 1 until parts.size) {
                    var w = parts[i].toLowerCase()
                    if (w.startsWith("_") || w.startsWith("/")) {
                        w = w.substring(1)
                    } else if (w.any { it.toString().defaultForm() !in CONSONANTS
                                    && VOWEL_FORM.none { v -> v eq it.toString() } }) {
                        glosses += error("Non-ithkuil characters detected in word '$w'")
                        continue
                    }
                    val res = try {
                        parseWord(w, prec, ignoreDefault, alone = true)
                    } catch (ex: Exception) {
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
                    MarkdownUtil.bold(parts[i+1] + " : ") + if (s.startsWith("\u0000")) {
                        MarkdownUtil.italics(s.substring(1, s.length))
                    } else {
                        s
                    }
                }.joinToString("\n", MarkdownUtil.underline("Gloss : ") + "\n")
                chan.sendMessage(newMessage)
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
                        .mapIndexed { i, s ->
                            if (i == 0) {
                                s.drop(first.length)
                            } else {
                                s
                            }
                        }
                        .map { parseSentence(it, prec, ignoreDefault) }
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
                val newMessage = MarkdownUtil.underline("Gloss :") + " " + if (sentences.startsWith("\u0000")) {
                    MarkdownUtil.italics(sentences.drop(1))
                } else {
                    sentences
                }
                chan.sendMessage(newMessage)
                        .queue()
            }
            "stop" -> {
                if (event.author.id in authorizedUsers) {
                    exitProcess(0)
                }
            }
        }
    }
}