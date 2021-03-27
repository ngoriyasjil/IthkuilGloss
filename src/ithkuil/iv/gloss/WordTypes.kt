package ithkuil.iv.gloss

fun wordTypeOf(word: Word): WordType {

    return when {
        word.size == 1 && word[0].isConsonant() -> WordType.BIAS_ADJUNCT

        word[0] == "hr" && word.size == 2 -> WordType.MOOD_CASESCOPE_ADJUNCT

        word[0] == "h" && word.size == 2 -> WordType.REGISTER_ADJUNCT

        (word[0].isVowel() || word[0] in setOf("w", "y"))
            && word.all { it.isVowel() || it in CN_CONSONANTS } -> WordType.MODULAR_ADJUNCT

        word.size >= 4 && word[0] == "ë" && word[3] in COMBINATION_REFERENTIAL_SPECIFICATION
            || word.size >= 3 && word[0] !in CC_CONSONANTS && word[2] in COMBINATION_REFERENTIAL_SPECIFICATION
            || word.size >= 4 && word[0] == "ï" && word[1] in CP_CONSONANTS
        -> WordType.COMBINATION_REFERENTIAL

        word.size in 2..3 && word[1].isConsonant() && word[1] !in CN_CONSONANTS && word[0] != "ë"
        -> WordType.AFFIXUAL_ADJUNCT

        word.size >= 5 && word[0].isConsonant() && ((if (word[1].endsWith("'")) "'" else "") + word[2]) in CZ_CONSONANTS
            || word.size >= 6 && (word[0] == "ë") && (((if (word[2].endsWith("'")) "'" else "") + word[3]) in CZ_CONSONANTS)
        -> WordType.MULTIPLE_AFFIX_ADJUNCT

        (word.last().isVowel() || word.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.last()
            ?.isVowel() == true)
            && word.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.dropLast(1)
            ?.all { it.isConsonant() || it == "ë" } == true
        -> WordType.REFERENTIAL

        else -> WordType.FORMATIVE
    }
}


/*

Consonant = "root consonant form" i.e. no "hn"

Bias A. = Pattern(Consonant)
Mood/Case-Scope A. = Pattern(In("hr"), Vowel)
Register A. = Pattern(In("h"), Vowel)
Modular A. = Pattern(Maybe("w", "y"), Repeat(Maybe(Pattern(Vowel, In(CN_CONSONANTS)))), Vowel)
Combination R. = Pattern(Maybe("ë"), Consonant, Vowel, In(COMBINATION_REFERENTIAL_SPECIFICATION), Continue)
              || Pattern(In("ï"), In(CP_CONSONANTS), Continue)
Affixual A. = Pattern(Not("ë", Vowel), Consonant, Maybe(Vowel))
Multiple Affix A


Alternate form:

Bias A. = matches(word) {
        consonant()
}

M/CS A. = matches(word) {
         in("hr")
         vowel()
}

Register A. = matches(word)  {
            in("h")
            vowel()
}

Modular A. = matches(word)  {
        maybe { in("w", "y") }
        for _ in 1..3 {
            maybe {
                vowel()
                in(CN_CONSONANTS)
            }
        }
        vowel()
}

Combination R. = matches(word) {
        maybe("ë") // = maybe { in("ë") }
        consonant()
        vowel()
        in(COMBINATION_REFERENTIAL_SPECIFICATION)
        maybe { continue() }
} or matches(word) {
        in("ï")
        in(CP_CONSONANTS)
        vowel()
        in(COMBINATION_REFERENTIAL_SPECIFICATION)
        maybe { continue() }
}

Affixual A. = pattern(word) {
         check { it != "ë" }
         vowel()
         consonant()
         maybe { vowel() }
}

Multiple Affix A. =

pattern(word) {
        maybe("ë")
        consonant()
        val czGlottal = current().endsWith("'")
        if (czGlottal) modify { it.removeSuffix("'") }
        vowel()
        if (czGlottal) modify { "'$it" }
        in(CZ_CONSONANTS)
        vowel()
        consonant()
        continue()
}

Referential

pattern(word) {
    maybe("ë")
    or( { consonant() }, { in(CP_CONSONANTS) } )
    while(current() == "ë") {
        in("ë")
        or( { consonant() }, { in(CP_CONSONANTS) } )
    }
    vowel()
    maybe {
        in("w", "y")
        vowel()
        maybe {
            or( { consonant() }, { in(CP_CONSONANTS) } )
            maybe("ë")
        }
    }
}




 */

fun pattern(word: Word, pattern : Matcher.() -> Unit) : Boolean {
    val matcher = Matcher(word)
    matcher.pattern()
    return matcher.matching
}

class Matcher(var slots : List<String>, var matching : Boolean = true) {
    fun or(first : Matcher.() -> Unit, second : Matcher.() -> Unit) {

    }
}



