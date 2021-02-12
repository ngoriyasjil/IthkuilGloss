package ithkuil.iv.gloss

import mu.KotlinLogging

var affixData: Map<String, AffixData> = emptyMap()
var rootData:  Map<String, RootData>  = emptyMap()

val logger = KotlinLogging.logger { }

fun String.stripPunctuation(): String = this.replace("[.,?!:;⫶`\"*]+".toRegex(), "")

fun String.isVowel() = with(substituteAll(UNSTRESSED_FORMS)) {
    when (length) {
        1, 2 -> all { it.toString() in VOWELS }
        3 -> this[1] == '\'' && this[0].toString() in VOWELS && this[2].toString() in VOWELS
        else -> false
    }
}

fun String.isConsonant() = this.all { it.toString().defaultForm() in CONSONANTS }

val STRESSED_VOWELS = setOf('á','â','é', 'ê', 'í', 'ô', 'ó', 'û', 'ú')

fun String.hasStress() : Boolean? = when {
    this.getOrNull(1) in STRESSED_VOWELS -> null
    this[0] in STRESSED_VOWELS -> true
    else -> false
}


fun String.withZeroWidthSpaces() = this.replace("([/—-])".toRegex(), "\u200b$1")

fun String.splitOnWhitespace() = this.split(Regex("\\p{javaWhitespace}")).filter { it.isNotEmpty() }
fun String.trimWhitespace() = this.splitOnWhitespace().joinToString(" ")


//Deals with series three vowels and non-default consonant forms
infix fun String.eq(s: String): Boolean = if ("/" in this) {
    this.split("/").any { it eq s }
} else {
    this.defaultForm() == s.defaultForm()
}

fun String.substituteAll(substitutions : List<Pair<String, String>>) = substitutions.fold(this) {
    current, (allo, sub) -> current.replace(allo.toRegex(), sub)
}

fun String.defaultFormWithStress() = toLowerCase().substituteAll(ALLOGRAPHS)

fun String.defaultForm() = defaultFormWithStress().substituteAll(UNSTRESSED_FORMS)


fun Array<String>.findStress(): Int? {
    val vowels = this.filter(String::isVowel)
        .flatMap {
            val (series, form) = seriesAndForm(it.defaultForm())
            if (series == 1 || series == 2 || (series == 3 && form == 5)) {
                listOf(it)
            } else {
                it.toCharArray().map(Char::toString).filter { ch -> ch != "'" }
            }
        }
    if (vowels.size == 1) return -1
    val stressIndex = vowels
        .reversed()
        .map { it.hasStress() ?: return null }
        .takeIf { list -> list.count { it } <= 1 }
        ?.indexOfFirst { it }
    return when (stressIndex) {
        -1 -> 1
        null -> null
        else -> stressIndex
    }
}

fun String.splitGroups(): Array<String> {
    val groups = arrayListOf<String>()
    var chars = toCharArray()
            .map(Char::toString)
            .toList()
    while (chars.isNotEmpty()) {
        val group = when {
            chars[0].defaultForm().isVowel() -> {
                if (chars.size >= 3 && (chars[0] + chars[1] + chars[2]).isVowel()) {
                    chars[0] + chars[1] + chars[2]
                } else if (chars.size >= 2 && (chars[0] + chars[1]).isVowel()) {
                    chars[0] + chars[1]
                } else {
                    chars[0]
                }
            }
            chars[0].isConsonant() -> {
                chars.takeWhile(String::isConsonant).joinToString("")
            }

            chars[0] == "-" -> chars[0]

            else -> {
                throw IllegalArgumentException("Non-Ithkuil character: ${chars[0]}")
            }
        }
        chars = chars.subList(group.length, chars.size)
        groups += group
    }
    return groups.toTypedArray()
}

data class AffixData(val abbreviation: String, val descriptions: List<String>)

fun parseAffixes(data: String): Map<String, AffixData> = data
        .lineSequence()
        .drop(1)
        .map       { it.split("\t") }
        .filter    { it.size >= 11 }
        .associate { it[0] to AffixData(it[1], it.subList(2, 11)) }

data class RootData(val descriptions: List<String>)

fun parseRoots(data: String): Map<String, RootData> = data
        .lineSequence()
        .drop(1)
        .map       { it.split("\t") }
        .filter    { it.size >= 5 }
        .associate { it[0] to RootData(it.subList(1, 5)) }
