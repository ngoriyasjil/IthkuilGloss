@file:OptIn(ExperimentalTime::class)

package ithkuil.iv.gloss.`interface`

import java.io.File
import java.net.URL
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import ithkuil.iv.gloss.*

val startTime = System.currentTimeMillis()

const val MORPHOPHONOLOGY_VERSION = "0.18.5"

const val AFFIXES_URL =
    "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516"
const val ROOTS_URL =
    "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303"

const val AFFIXES_PATH = "./resources/affixes.tsv"
const val ROOTS_PATH = "./resources/roots.tsv"

fun loadResourcesOnline() {
    logger.info { "-> loadResourcesOnline()    (${affixData.size} affixes, ${rootData.size} roots)" }
    val affixes = URL(AFFIXES_URL).readText()
    val roots = URL(ROOTS_URL).readText()

    File(AFFIXES_PATH).writeText(affixes)
    File(ROOTS_PATH).writeText(roots)

    affixData = parseAffixes(affixes)
    rootData = parseRoots(roots)
    logger.info { "   loadResourcesOnline() -> (${affixData.size} affixes, ${rootData.size} roots)" }
}

fun loadResourcesLocal() {
    logger.info { "-> loadResourcesLocal()    (${affixData.size} affixes, ${rootData.size} roots)" }
    val affixes = File(AFFIXES_PATH).readText()
    val roots = File(ROOTS_PATH).readText()

    affixData = parseAffixes(affixes)
    rootData = parseRoots(roots)
    logger.info { "   loadResourcesLocal() -> (${affixData.size} affixes, ${rootData.size} roots)" }
}

fun requestPrecision(request: String) = when {
    request.contains("short") -> Precision.SHORT
    request.contains("full") -> Precision.FULL
    else -> Precision.REGULAR
}

fun respond(content: String, maybeLastMessage: (() -> String?)? = null): String? {
    if (!content.startsWith("?")) {
        return Regex(":\\?(.*?)\\?:", RegexOption.DOT_MATCHES_ALL).findAll(content)
            .map { match -> respond("?sshort ${match.groupValues[1].trimWhitespace()}") }
            .joinToString("\n\n")
    }

    val (fullRequest, remainder) = content.splitOnWhitespace().let { Pair(it[0], it.drop(1)) }
    val request = fullRequest.removePrefix("??").removePrefix("?")
    val o = GlossOptions(requestPrecision(request), fullRequest.startsWith("??"))
    logger.info { "   respond($content) received options: $o" }

    val arguments = if(remainder.size == 0) {
        maybeLastMessage?.invoke()?.splitOnWhitespace()
    } else { null } ?: remainder;
    logger.info { "   respond($content) received arguments: ${
        arguments.mapIndexed { index, it -> "$index: $it" }}}" }

    return when (request) {
        "gloss", "short", "full" -> wordByWord(arguments, o)

        "s", "sgloss", "sshort", "sfull" -> sentenceGloss(arguments, o)

        "root", "affix" -> when (arguments.size) {
            1 -> {
                val lookup = arguments[0].trim('-').toLowerCase()
                val (consonantalForm, generalDescription, details) = when (request) {
                    "root" -> rootData[lookup]?.let { root ->
                        Triple(
                            "-$lookup-",
                            root.descriptions[0],
                            root.descriptions.drop(1)
                        )
                    }
                    "affix" -> affixData[lookup]?.let { affix -> Triple("-$lookup", affix.abbr, affix.desc) }
                    else -> /* unreachable */ null
                } ?: return "$lookup not found"

                return "$request **$consonantalForm**: $generalDescription\n" +
                        details.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
            }
            else -> "*Please enter exactly one root or affix.*"
        }

        "!stop" -> exitProcess(0)

        "!reload" -> try {
            loadResourcesOnline()
            "External resources successfully reloaded!"
        } catch (e: Exception) {
            logger.info { e.toString() }
            "Error while reloading external resources…"
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
                "**Uptime:** ${(System.currentTimeMillis() - startTime).milliseconds}",
                "**Last commit:** $lastCommit"
            ).joinToString("\n")
        }

        "!whosagoodbot", "!whosacutebot" -> "(=^ェ^=✿)"

        else -> null
    }
}

fun sentenceGloss(words: List<String>, o: GlossOptions): String {
    val glosses = words.map {
        it to (try {
            parseWord(it.stripPunctuation()) as? Gloss
        } catch (ex: Exception) {
            logger.info { ex.toString() }
            null
        })
    }.map { (word, gloss) ->
        gloss?.toString(o)?.withZeroWidthSpaces() ?: "**$word**"
    }

    return glosses.joinToString(" ")
}

fun wordByWord(words: List<String>, o: GlossOptions): String {
    val glossPairs = words
        .map(String::stripPunctuation)
        .map { word ->
            val gloss = try {
                parseWord(word)
            } catch (ex: Exception) {
                logger.info { ex.toString() }
                Error("A severe exception occurred. Please contact the maintainers.")
            }
            word to gloss
        }.map { (word, gloss) ->
            word to when (gloss) {
                is Error -> "*${gloss.message}*"
                is Gloss -> gloss.toString(o)
            }
        }

    return glossPairs.joinToString("\n") { (word, gloss) -> "**$word**: $gloss" }

}

