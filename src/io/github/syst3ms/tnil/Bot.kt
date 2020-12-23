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

const val MORPHOPHONOLOGY_VERSION = "0.18.3"

const val AFFIXES_URL = "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516"
const val ROOTS_URL = "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303"

const val AFFIXES_PATH = "./resources/affixes.tsv"
const val ROOTS_PATH = "./resources/roots.tsv"

fun loadResourcesOnline() {
    logger.info("-> loadResourcesOnline()    ({} affixes, {} roots)", affixData.size, rootData.size)
    val affixes = URL(AFFIXES_URL).readText()
    val roots = URL(ROOTS_URL).readText()

    File(AFFIXES_PATH).writeText(affixes)
    File(ROOTS_PATH).writeText(roots)

    affixData = parseAffixes(affixes)
    rootData = parseRoots(roots)
    logger.info("   loadResourcesOnline() -> ({} affixes, {} roots)", affixData.size, rootData.size)
}

fun loadResourcesLocal() {
    logger.info("-> loadResourcesLocal()    ({} affixes, {} roots)", affixData.size, rootData.size)
    val affixes = File(AFFIXES_PATH).readText()
    val roots = File(ROOTS_PATH).readText()

    affixData = parseAffixes(affixes)
    rootData = parseRoots(roots)
    logger.info("   loadResourcesLocal() -> ({} affixes, {} roots)", affixData.size, rootData.size)
}

fun requestPrecision(request: String) = when {
    request.contains("short") -> 0
    request.contains("full")  -> 2
    request.contains("debug") -> 3
    else                      -> 1
}

fun respond(content: String) : String? {
    val (fullRequest, arguments) = content.split("\\s+".toRegex()).let { Pair(it[0], it.drop(1)) }
    val ignoreDefault = !fullRequest.startsWith("??")
    val request = fullRequest.removePrefix("??").removePrefix("?")
    val precision = requestPrecision(request)

    when(request) {
        "gloss", "short", "full", "!debug" -> return wordByWord(arguments, precision, ignoreDefault)

        "s", "sgloss", "sshort", "sfull", "!sdebug" -> return sentenceGloss(arguments, precision, ignoreDefault)
        
        "root", "affix" -> when(arguments.size) {
            1 -> {
                val lookup = arguments[0].trim('-').toLowerCase()
                val (consonantalForm, generalDescription, details) = when(request) {
                    "root"  ->  rootData.get(lookup)?.let {  root -> Triple("-$lookup-", root.descriptions[0], root.descriptions.drop(1)) }
                    "affix" -> affixData.get(lookup)?.let { affix -> Triple("-$lookup", affix.abbr, affix.desc) }
                    else    -> /* unreachable */ null
                } ?: return "$lookup not found";

                return "$request **$consonantalForm**: $generalDescription\n" +
                    details.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
            }
            else -> return "*Please enter exactly one root or affix.*"
        }

        "!stop" -> exitProcess(0)

        "!reload" -> {
            return try {
                loadResourcesOnline()
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
    val glosses = words.map { word ->
        val gloss = try {
            when (val parse = parseWord(word.stripPunctuation())) {
                is Error -> null
                is Gloss -> parse
            }
        } catch (ex: Exception) {
            logger.error("{}", ex)
            null
        }
        word to gloss
    }.map { (word, gloss) ->
        gloss?.toString(precision, ignoreDefault)?.withZeroWidthSpaces() ?: "**$word**"
    }

    return "__Gloss__:\n" +
            glosses.joinToString(" ")
}

fun wordByWord(words: List<String>, precision: Int, ignoreDefault: Boolean): String {
    val glossPairs = words
        .map(String::stripPunctuation)
        .map { word ->
            val gloss = try {
                parseWord(word)
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
            word to when (gloss) {
                is Error -> "*${gloss.message}*"
                is Gloss -> gloss.toString(precision, ignoreDefault)
            }
        }

    return "__Gloss__:\n" +
            glossPairs.joinToString("\n") { (word, gloss) -> "**$word:** $gloss" }

}

