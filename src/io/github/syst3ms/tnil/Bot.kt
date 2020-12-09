@file:OptIn(ExperimentalTime::class)

package io.github.syst3ms.tnil

import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

val logger = LoggerFactory.getLogger("tnilgloss")!!

val leStart = System.currentTimeMillis()

const val MORPHOPHONOLOGY_VERSION = "0.17.2"

fun loadResources() {
    affixData = parseAffixes(URL("https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516").readText())
    rootData = parseRoots(URL("https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303").readText())
}

fun parsePrecision(request: String) = when {
    request.contains("short") -> 0
    request.contains("full")  -> 2
    request.contains("debug") -> 3
    else                      -> 1
}

fun respond(content: String) : String? {
    var request = content.split("\\s+".toRegex())[0]
    val ignoreDefault = !request.startsWith("??")
    request = request.removePrefix("??").removePrefix("?")
    val precision = parsePrecision(request)

    when(request) {

        "gloss", "short", "full", "!debug" -> return wordByWord(content, precision, ignoreDefault)

        "s", "sgloss", "sshort", "sfull", "!sdebug" -> return sentenceGloss(content, request, precision, ignoreDefault)

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
            val lastCommit = String(git.inputStream.readBytes())
            return listOf(
                "__Status report:__",
                "**Ithkuil Version:** $MORPHOPHONOLOGY_VERSION",
                "**Roots:** ${rootData.size}",
                "**Affixes:** ${affixData.size}",
                "**Help file exists:** ${File("./resources/help.md").exists()}",
                "**Uptime:** ${(System.currentTimeMillis() - leStart).milliseconds}",
                "**Last commit:** $lastCommit"
            ).joinToString("\n")
        }

        "!whosagoodbot", "!whosacutebot" -> return "(=^ェ^=✿)"

        else -> return null
    }
}

private fun wordByWord(content: String, prec: Int, ignoreDefault: Boolean): String {
    val words = content.split("[\\s.;,:?!]+".toRegex()).filter(String::isNotBlank).drop(1)
    val glosses = arrayListOf<String>()
    for (word in words) {
        var w = word.toLowerCase().replace("’", "'").replace("\u200b", "")
        if (w.startsWith("_") || w.startsWith("/")) {
            w = w.substring(1)
        } else {

            val nonIthkuil = w.defaultForm().filter {
                it.toString().defaultForm() !in ITHKUIL_CHARS
            }
            if (nonIthkuil.isNotEmpty()) {
                glosses += error(
                    "Non-ithkuil characters detected: " +
                            nonIthkuil.map { "\"$it\" (" + it.toInt().toString(16) + ")" }.joinToString() +
                            if (nonIthkuil.contains("[qˇ^ʰ]".toRegex())) " You might be writing in Ithkuil III. Try \"!gloss\" instead." else ""
                )
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
        "**${words[i]}**: " + if (s.startsWith("\u0000")) {
            "*${s.drop(1)}*"
        } else {
            s
        }
    }.joinToString("\n", "__Gloss:__\n") + "\n"

    return newMessage.withZeroWidthSpaces()
}

private fun sentenceGloss(content: String, request: String, prec: Int, ignoreDefault: Boolean): String {
    val sentences = content.split("\\s*\\.\\s*".toRegex())
        .asSequence()
        .filter(String::isNotBlank)
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
        .reduce { acc, s ->
            when {
                acc.startsWith("\u0000") -> acc
                s.startsWith("\u0000") -> s
                else -> "$acc  //  $s"
            }
        }
    val newMessage = "__Gloss:__\n" + if (sentences.startsWith("\u0000")) {
        "*${sentences.drop(1)}*"
    } else {
        sentences
    }
    return newMessage.withZeroWidthSpaces()
}

