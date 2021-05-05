@file:OptIn(ExperimentalTime::class)

package ithkuil.iv.gloss.dispatch

import ithkuil.iv.gloss.*
import kotlinx.datetime.Clock
import mu.KotlinLogging
import java.io.File
import java.net.URL
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

val startTime = Clock.System.now()

val logger = KotlinLogging.logger { }


fun parseAffixes(data: String): Map<String, AffixData> = data
    .lineSequence()
    .drop(1)
    .map { it.split("\t") }
    .filter { it.size >= 11 }
    .associate { it[0] to AffixData(it[1], it.subList(2, 11)) }

fun parseRoots(data: String): Map<String, RootData> = data
    .lineSequence()
    .drop(1)
    .map { it.split("\t") }
    .filter { it.size >= 5 }
    .associate { it[0] to RootData(it.subList(1, 5)) }

object LocalDictionary : Resources {

    var affixes: Map<String, AffixData> = emptyMap()
    var roots: Map<String, RootData> = emptyMap()

    override fun getAffix(cs: String): AffixData? = affixes[cs]

    override fun getRoot(cr: String): RootData? = roots[cr]
}

const val MORPHOPHONOLOGY_VERSION = "0.19.0"

const val AFFIXES_URL =
    "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516"
const val ROOTS_URL =
    "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303"

const val AFFIXES_PATH = "./resources/affixes.tsv"
const val ROOTS_PATH = "./resources/roots.tsv"

fun loadResourcesOnline() = with(LocalDictionary) {
    logger.info { "-> loadResourcesOnline()    (${affixes.size} affixes, ${roots.size} roots)" }
    val loadedAffixes = URL(AFFIXES_URL).readText()
    val loadedRoots = URL(ROOTS_URL).readText()

    File(AFFIXES_PATH).writeText(loadedAffixes)
    File(ROOTS_PATH).writeText(loadedRoots)

    affixes = parseAffixes(loadedAffixes)
    roots = parseRoots(loadedRoots)
    logger.info { "   loadResourcesOnline() -> (${affixes.size} affixes, ${roots.size} roots)" }
}

fun loadResourcesLocal() = with(LocalDictionary) {
    logger.info { "-> loadResourcesLocal()    (${affixes.size} affixes, ${roots.size} roots)" }
    val loadedAffixes = File(AFFIXES_PATH).readText()
    val loadedRoots = File(ROOTS_PATH).readText()

    affixes = parseAffixes(loadedAffixes)
    roots = parseRoots(loadedRoots)
    logger.info { "   loadResourcesLocal() -> (${affixes.size} affixes, ${roots.size} roots)" }
}

fun requestPrecision(request: String) = when {
    request.contains("short") -> Precision.SHORT
    request.contains("full") -> Precision.FULL
    else -> Precision.REGULAR
}

fun respond(content: String): String? {
    if (!content.startsWith("?")) {
        return Regex(":\\?(.*?)\\?:", RegexOption.DOT_MATCHES_ALL).findAll(content)
            .map { match -> respond("?sshort ${match.groupValues[1].trimWhitespace()}") }
            .joinToString("\n\n")
            .also {
                if (it.isBlank()) return null
            }
    }

    val (fullRequest, arguments) = content.splitOnWhitespace().let { it[0] to it.drop(1) }
    val request = fullRequest.removePrefix("??").removePrefix("?")
    val o = GlossOptions(requestPrecision(request), fullRequest.startsWith("??"))
    logger.info { "   respond($content) received options: $o" }
    logger.info {
        "   respond($content) received arguments: ${
            arguments.mapIndexed { index, it -> "$index: $it" }
        }"
    }

    return when (request) {
        "gloss", "short", "full" -> wordByWord(arguments, o)

        "s", "sgloss", "sshort", "sfull" -> sentenceGloss(arguments, o)

        "root" -> lookupRoot(arguments)

        "affix" -> lookupAffix(arguments)

        "!stop" -> exitProcess(0)

        "!reload" -> try {
            loadResourcesOnline()
            "External resources successfully reloaded!"
        } catch (e: Exception) {
            logger.error { e.toString() }
            "Error while reloading external resources. Please contact the maintainers"
        }

        "status" -> """
            __Status report:__
            **Ithkuil Version:** $MORPHOPHONOLOGY_VERSION
            **Roots:** ${LocalDictionary.roots.size}
            **Affixes:** ${LocalDictionary.affixes.size}
            **Help file exists:** ${File("./resources/help.md").exists()}
            **Uptime:** ${Clock.System.now() - startTime}
            **Last commit:** $lastCommit
        """.trimIndent()

        "ej" -> externalJuncture(arguments.formatAll())

        "whosagoodbot", "whosacutebot" -> "(=^ェ^=✿)"

        "date" -> datetimeInIthkuil()

        else -> null
    }
}

val lastCommit: String by lazy {
    ProcessBuilder("git", "log", "-1", "--oneline")
        .start()
        .inputStream
        .bufferedReader()
        .readText()
}

fun lookupRoot(crs: List<String>): String {
    val lookups = crs.map { it.removeSuffix(",").trim('-').defaultForm() }

    val entries = mutableListOf<String>()

    for (cr in lookups) {
        val root = LocalDictionary.roots[cr]
        if (root != null) {
            val generalDescription = root[Stem.ZERO]
            val stemDescriptions = root.descriptions.drop(1)
            val titleLine = "**-${cr.toUpperCase()}-**: $generalDescription"

            val descLines = stemDescriptions.mapIndexedNotNull { index, description ->
                if (description.isNotEmpty()) "${index + 1}. $description"
                else null
            }.joinToString("\n")

            entries.add(
                "$titleLine\n$descLines"
            )

        } else {
            entries.add("*-${cr.toUpperCase()}- not found*")
        }

    }

    return entries.joinToString("\n\n")

}

fun lookupAffix(cxs: List<String>): String {
    val lookups = cxs.map { it.removeSuffix(",").trim('-').defaultForm() }

    val entries = mutableListOf<String>()

    for (cx in lookups) {
        val affix = LocalDictionary.affixes[cx]
        if (affix != null) {
            val abbreviation = affix.abbreviation
            val degreeDescriptions = affix.descriptions
            val titleLine = "**-$cx**: $abbreviation"

            val descLines = degreeDescriptions.mapIndexedNotNull { index, description ->
                if (description.isNotEmpty()) "${index + 1}. $description"
                else null
            }.joinToString("\n")

            entries.add(
                "$titleLine\n$descLines"
            )

        } else {
            entries.add("*-$cx not found*")
        }

    }

    return entries.joinToString("\n\n")
}


fun sentenceGloss(words: List<String>, o: GlossOptions): String {
    val glosses = glossInContext(words.formatAll())
        .map { (word, parsed) ->
            when (parsed) {
                is Foreign -> "*$word*"
                is Error -> "**$word**"
                is Parsed -> {
                    parsed.checkDictionary(LocalDictionary)
                    parsed.gloss(o).withZeroWidthSpaces()
                }
            }
        }

    return glosses.joinToString(" ")
}

fun wordByWord(words: List<String>, o: GlossOptions): String {
    val glossPairs = glossInContext(words.formatAll())
        .map { (word, parsed) ->
            when (parsed) {
                is Foreign -> "**$word**"
                is Error -> "**$word:** *${parsed.message}*"
                is Parsed -> {
                    parsed.checkDictionary(LocalDictionary)
                    "**$word:** ${parsed.gloss(o)}"
                }
            }
        }

    return glossPairs.joinToString("\n")

}

fun String.withZeroWidthSpaces() = replace("[/—-]".toRegex(), "\u200b$0")

fun String.splitOnWhitespace() = split(Regex("\\p{javaWhitespace}")).filter { it.isNotEmpty() }

fun String.trimWhitespace() = splitOnWhitespace().joinToString(" ")