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

fun requestPrecision(request: String) = when {
    request.contains("short") -> 0
    request.contains("full")  -> 2
    request.contains("debug") -> 3
    else                      -> 1
}

fun respond(content: String) : String? {
    val words = content.split("\\s+".toRegex())
    var request = words[0]
    val ignoreDefault = !request.startsWith("??")
    request = request.removePrefix("??").removePrefix("?")
    val precision = requestPrecision(request)



    when(request) {

        "gloss", "short", "full", "!debug" -> return wordByWord(words.drop(1), precision, ignoreDefault)

        "s", "sgloss", "sshort", "sfull", "!sdebug" -> return sentenceGloss(words.drop(1), precision, ignoreDefault)

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

fun sentenceGloss(words: List<String>, precision: Int, ignoreDefault: Boolean): String {
    val glossPairs = words.map { word ->

        val gloss = try {
            val parse = parseWord(word.stripPunctuation(), precision, ignoreDefault)
            if (parse.startsWith("\u0000")) null
            else parse

        } catch (ex: Exception) {
            logger.error("{}", ex)
            null
        }
        word to gloss
    }.map { (word, gloss) ->
        gloss?.withZeroWidthSpaces() ?: "**$word**"
    }

    return "__Gloss__:\n" +
            glossPairs.joinToString(" ")
}

fun wordByWord(words: List<String>, precision: Int, ignoreDefault: Boolean): String {
    val glossPairs = words
        .map(String::stripPunctuation)
        .map { word ->

        val gloss = try {
            parseWord(word, precision, ignoreDefault)
        } catch (ex: Exception) {
            logger.error("{}", ex)
            if (precision < 3) {
                Error("A severe exception occurred. Please contact the maintainers.")
            } else {
                val sw = StringWriter()
                ex.printStackTrace(PrintWriter(sw))
                val stacktrace = sw.toString()
                    .split("\n")
                    .take(10)
                    .joinToString("\n")
                Error(stacktrace)
            }

        }
        word to gloss
    }.map { (word, gloss) ->

        if (gloss.startsWith("\u0000"))
            word to "*${gloss.drop(1)}*"
        else
            word to gloss.withZeroWidthSpaces()
    }

    return "__Gloss__:\n" +
            glossPairs.joinToString("\n") { (word, gloss) -> "**$word:** $gloss" }

}

