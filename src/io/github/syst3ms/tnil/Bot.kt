@file:OptIn(ExperimentalTime::class)

package io.github.syst3ms.tnil

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

val leStart = System.currentTimeMillis()

const val MORPHOPHONOLOGY_VERSION = "0.18.3"

const val AFFIXES_URL = "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=499365516"
const val ROOTS_URL = "https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/export?format=tsv&gid=1534088303"

const val AFFIXES_PATH = "./resources/affixes.tsv"
const val ROOTS_PATH = "./resources/roots.tsv"

fun loadResourcesOnline() {
    "-> loadResourcesOnline()    (${affixData.size} affixes, ${rootData.size} roots)".log()
    val affixes = URL(AFFIXES_URL).readText()
    val roots = URL(ROOTS_URL).readText()

    File(AFFIXES_PATH).writeText(affixes)
    File(ROOTS_PATH).writeText(roots)

    affixData = parseAffixes(affixes)
    rootData = parseRoots(roots)
    "   loadResourcesOnline() -> (${affixData.size} affixes, ${rootData.size} roots)".log()
}

fun loadResourcesLocal() {
    "-> loadResourcesLocal()    (${affixData.size} affixes, ${rootData.size} roots)".log()
    val affixes = File(AFFIXES_PATH).readText()
    val roots = File(ROOTS_PATH).readText()

    affixData = parseAffixes(affixes)
    rootData = parseRoots(roots)
    "   loadResourcesLocal() -> (${affixData.size} affixes, ${rootData.size} roots)".log()
}

fun requestPrecision(request: String) = when {
    request.contains("short") -> Precision.SHORT
    request.contains("full")  -> Precision.FULL
    request.contains("debug") -> Precision.DEBUG
    else                      -> Precision.REGULAR
}

fun respond(content: String) : String? {
    val (fullRequest, arguments) = content.split("\\s+".toRegex()).let { Pair(it[0], it.drop(1)) }
    val request = fullRequest.removePrefix("??").removePrefix("?")
    val o = GlossOpts(requestPrecision(request), fullRequest.startsWith("??"))
    "   respond($content) got opts: $o".log()

    when(request) {
        "gloss", "short", "full", "!debug" -> return wordByWord(arguments, o)

        "s", "sgloss", "sshort", "sfull", "!sdebug" -> return sentenceGloss(arguments, o)
        
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
                e.toString().log()
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

fun sentenceGloss(words: List<String>, o: GlossOpts): String {
    val glosses = words.map {
        it to (try {
            parseWord(it.stripPunctuation()) as? Gloss
        } catch (ex: Exception) {
            ex.toString().log()
            null
        })
    }.map { (word, gloss) ->
        gloss?.toString(o)?.withZeroWidthSpaces() ?: "**$word**"
    }

    return "__Gloss__:\n" +
            glosses.joinToString("\u2003")
}

fun wordByWord(words: List<String>, o: GlossOpts): String {
    val glossPairs = words
        .map(String::stripPunctuation)
        .map { word ->
            val gloss = try {
                parseWord(word)
            } catch (ex: Exception) {
                ex.toString().log()
                when {
                    o.debug -> Error("A severe exception occurred. Please contact the maintainers.")
                    else -> {
                        val sw = StringWriter()
                        ex.printStackTrace(PrintWriter(sw))
                        val stacktrace = sw.toString()
                            .split("\n")
                            .take(10)
                            .joinToString("\n")
                        Error(stacktrace)
                    }
                }
            }
            word to gloss
        }.map { (word, gloss) ->
            word to when (gloss) {
                is Error -> "*${gloss.message}*"
                is Gloss -> gloss.toString(o)
            }
        }

    return "__Gloss__:\n" +
            glossPairs.joinToString("\n") { (word, gloss) -> "**$word:** $gloss" }

}

