package ithkuil.iv.gloss

import java.lang.Exception

fun glossInContext(words: List<String>) : List<Pair<String, GlossOutcome>> {
    val glossPairs = mutableListOf<Pair<String, GlossOutcome>>()

    var withinQuotes = false
    var followsCarrier = false
    var terminatedPhrase = false

    for ((index, word) in words.withIndex()) {

        val gloss : GlossOutcome

        if (followsCarrier && word matches "^[:⫶].+".toRegex()) withinQuotes = true
        if (terminatedPhrase && word == "hü") terminatedPhrase = false


        if (!followsCarrier && !withinQuotes && !terminatedPhrase) {
            if (isCarrier(word)) {
                followsCarrier = true

                val hasTerminator = words.subList(index+1, words.size)
                    .takeWhile { it.last() !in setOf('.', '?', '!') }
                    .any { isTerminator(it) }

                if (hasTerminator) terminatedPhrase = true

            }

            val nextFormativeIsVerbal : Boolean? by lazy {
                words.subList(index+1, words.size)
                    .takeWhile { it.last() !in setOf('.', '?', '!') }
                    .find { wordTypeOf(it.defaultForm().splitGroups()) == WordType.FORMATIVE }
                    ?.let {
                        it.defaultFormWithStress().splitGroups().findStress() in setOf(0, -1)
                    }
            }

            gloss = try {
                parseWord(word, marksMood = nextFormativeIsVerbal)
            } catch (ex: Exception) {
                logger.error("", ex)
                Error("A severe exception occurred. Please contact the maintainers.")
            }

        } else {

            gloss = Foreign(word)

            if (followsCarrier) followsCarrier = false
            if (isTerminator(word)) terminatedPhrase = false
            if (withinQuotes && word matches ".+[:⫶]$".toRegex()) withinQuotes = false
        }

        glossPairs.add(word.defaultFormWithStress() to gloss)

    }

    return glossPairs

}

private fun isTerminator(word: String) = word == "hü" || word.startsWith(LOW_TONE_MARKER)

fun isCarrier(word: String) : Boolean {

    if ('-' in word) {
        return word.split('-').any { isCarrier(it) }
    }

    val groups = try {
         word.defaultForm().splitGroups()
    } catch (_ : IllegalArgumentException) {
        return false // This is a hack until I clean up the formatting logic
    }

    return when (wordTypeOf(groups)) {
        WordType.FORMATIVE -> {
            val rootIndex = when {
                groups.getOrNull(0) in CC_CONSONANTS -> 2
                groups.getOrNull(0)?.isVowel() == true -> 1
                else -> 0
            }

            if (groups.getOrNull(rootIndex-1) in SPECIAL_VV_VOWELS) return false

            groups.getOrNull(rootIndex) == CARRIER_ROOT_CR
        }
        WordType.REFERENTIAL, WordType.COMBINATION_REFERENTIAL ->
            setOf("hl", "hn", "hň").any { it in groups } //Quotative adjunct "hm" is not included

        else -> false
    }
}