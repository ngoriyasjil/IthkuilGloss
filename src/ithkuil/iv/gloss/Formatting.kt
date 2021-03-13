package ithkuil.iv.gloss

sealed class FormattingOutcome

class Invalid(private val word : String, val message: String) : FormattingOutcome() {
    override fun toString(): String = word
}


class Word
private constructor(
    private val stressedGroups: List<String>,
    val stress: Int,
    val prefixPunctuation: String = "",
    val postfixPunctuation: String = "",
    val groups : List<String> = stressedGroups.map { it.substituteAll(UNSTRESSED_FORMS) }
    // ^ Should never be specified; class delegation doesn't accept properties, only parameters
) : List<String> by groups, FormattingOutcome() {

    override fun toString() : String {
        return stressedGroups.joinToString("", prefix = prefixPunctuation, postfix = postfixPunctuation)
    }

    companion object {

        fun from(s: String) : FormattingOutcome {

            if (s.isEmpty()) return Invalid(s, "Empty word")

            val punct = ".,?!:;⫶`\"*_"
            val punctuationRegex = "^([$punct]*)([^$punct]+)([$punct]*)$".toRegex()

            val (prefix, word, postfix) = punctuationRegex.find(s)?.destructured
                ?: return Invalid(s, "Unexpected punctuation")

            val clean = word.defaultFormWithStress()

            val nonIthkuil = clean.filter { it.toString() !in ITHKUIL_CHARS }
            if (nonIthkuil.isNotEmpty()) {
                var message = nonIthkuil.map { codepointString(it) }.joinToString()

                if ("[qˇ^ʰ]".toRegex() in nonIthkuil) {
                    message += " You might be writing in Ithkuil III. Try \"!gloss\" instead."
                }
                return Invalid(s, "Non-ithkuil characters detected: $message")
            }

            val stressedGroups = clean.splitGroups() ?: return Invalid(s, "Failed grouping")

            val stress = findStress(stressedGroups) ?: return Invalid(s, "Unknown stress")

            val groups = stressedGroups.map { it.substituteAll(UNSTRESSED_FORMS) }

            return Word(groups, stress, prefixPunctuation = prefix, postfixPunctuation = postfix)
        }

        private fun codepointString(c : Char): String {
            val codepoint = c.toInt()
                .toString(16)
                .toUpperCase()
                .padStart(4, '0')
            return "\"$c\" (U+$codepoint)"
        }
    }
}

enum class GroupingState {
    VOWEL,
    CONSONANT;

    fun switch(): GroupingState = when (this) {
        CONSONANT -> VOWEL
        VOWEL -> CONSONANT
    }
}

fun String.splitGroups(): List<String>? {
    val groups = mutableListOf<String>()

    val chars = map(Char::toString) //Change to a list of strings for historical reasons

    var index = 0

    var state = if (chars[index] in VOWELS) GroupingState.VOWEL else GroupingState.CONSONANT

    while (index <= lastIndex) {
        val group : String

        if (chars[index] == "-") {
            state = if (chars[index+1] in VOWELS) GroupingState.VOWEL else GroupingState.CONSONANT
            group = "-"
        } else {
            val cluster = when (state) {
                GroupingState.CONSONANT -> chars.subList(index, length)
                    .takeWhile { it in CONSONANTS }
                    .joinToString("")
                GroupingState.VOWEL -> chars.subList(index, length)
                    .takeWhile { it in VOWELS_AND_GLOTTAL_STOP }
                    .joinToString("")
            }

            if (cluster.isEmpty()) return null

            if (state == GroupingState.VOWEL && !cluster.isVowel()) return null

            state = state.switch()
            group = cluster
        }


        index += group.length
        groups += group
    }

    return groups
}

//Matches strings of the form "a", "ai", "a'" "a'i" and "ai'". Doesn't guarantee a valid vowelform.
fun String.isVowel() = when (length) {
    1 -> this in VOWELS
    2 -> this[0].toString() in VOWELS && this[1].toString() in VOWELS_AND_GLOTTAL_STOP
    3 -> all { it.toString() in VOWELS_AND_GLOTTAL_STOP } && this[0] != '\'' && this.count { it == '\'' } == 1
    else -> false
}

fun String.stripPunctuation(): String = this.replace("[.,?!:;⫶`\"*_]+".toRegex(), "")



fun String.isConsonant() = this.all { it.toString() in CONSONANTS }

val STRESSED_VOWELS = setOf('á','â','é', 'ê', 'í', 'î', 'ô', 'ó', 'û', 'ú')

fun String.hasStress() : Boolean? = when {
    this.getOrNull(1) in STRESSED_VOWELS -> null
    this[0] in STRESSED_VOWELS -> true
    else -> false
}


fun String.withZeroWidthSpaces() = this.replace("([/—-])".toRegex(), "\u200b$1")

fun String.splitOnWhitespace() = this.split(Regex("\\p{javaWhitespace}")).filter { it.isNotEmpty() }
fun String.trimWhitespace() = this.splitOnWhitespace().joinToString(" ")


//Deals with series three vowels
infix fun String.isSameVowelAs(s: String): Boolean = if ("/" in s) {
    s.split("/").any { it isSameVowelAs this }
} else {
    s == this
}

fun String.substituteAll(substitutions : List<Pair<String, String>>) = substitutions.fold(this) {
        current, (allo, sub) -> current.replace(allo.toRegex(), sub)
}

fun String.defaultFormWithStress() = stripPunctuation().toLowerCase().substituteAll(ALLOGRAPHS)

fun String.defaultForm() = defaultFormWithStress().substituteAll(UNSTRESSED_FORMS)


fun findStress(groups: List<String>): Int? {
    val vowels = groups.filter(String::isVowel)
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



