package ithkuil.iv.gloss

import java.lang.Exception
import ithkuil.iv.gloss.dispatch.logger

fun glossInContext(words: List<FormattingOutcome>) : List<Pair<String, GlossOutcome>> {
    val glossPairs = mutableListOf<Pair<String, GlossOutcome>>()

    var withinQuotes = false
    var followsCarrier = false
    var terminatedPhrase = false

    for ((index, maybeWord) in words.withIndex()) {

        val gloss: GlossOutcome

        val word: Valid = when (maybeWord) {
            is Invalid -> {
                glossPairs.add(maybeWord.toString() to Error(maybeWord.message))
                continue
            }
            is Valid -> maybeWord
        }

        if (followsCarrier && word.prefixPunctuation matches "[:⫶]".toRegex()) withinQuotes = true
        if (terminatedPhrase && word as? Word == listOf("h", "ü")) terminatedPhrase = false


        if (!followsCarrier && !withinQuotes && !terminatedPhrase) {
            if (isCarrier(word)) {
                followsCarrier = true

                val hasTerminator = words.subList(index + 1, words.size)
                    .mapNotNull { it as? Valid }
                    .takeWhile { !(it.postfixPunctuation matches "[.?!]+".toRegex()) }
                    .any { isTerminator(it) }

                if (hasTerminator) terminatedPhrase = true

            }

            val nextFormativeIsVerbal : Boolean? by lazy {
                words.subList(index+1, words.size)
                    .mapNotNull { it as? Valid }
                    .takeWhile { !(it.postfixPunctuation matches "[.?!]+".toRegex()) }
                    .find { when (it) {
                        is ConcatenatedWords -> true
                        is Word -> it.wordType == WordType.FORMATIVE }
                    }
                    ?.let { isVerbal(it) }
            }

            gloss = try {

                when (word) {
                    is Word -> parseWord(word, marksMood = nextFormativeIsVerbal)
                    is ConcatenatedWords -> parseConcatenationChain(word)
                }

            } catch (ex: Exception) {
                logger.error("", ex)
                Error("A severe exception occurred. Please contact the maintainers.")
            }

        } else {

            gloss = Foreign(word.toString())

            if (followsCarrier) followsCarrier = false
            if (isTerminator(word)) terminatedPhrase = false
            if (withinQuotes && word.postfixPunctuation matches "[:⫶]".toRegex()) withinQuotes = false
        }

        glossPairs.add(word.toString() to gloss)

    }

    return glossPairs

}

fun isVerbal(word: Valid) : Boolean = when (word) {
    is ConcatenatedWords -> isVerbal(word.words.last())
    is Word -> {
        (word.wordType == WordType.FORMATIVE)
            && (word.stress in setOf(Stress.ULTIMATE, Stress.MONOSYLLABIC))
    }
}

private fun isTerminator(word: Valid) = word == listOf("h","ü") || word.prefixPunctuation == LOW_TONE_MARKER

fun isCarrier(word: Valid) : Boolean {

    return when (word) {
        is ConcatenatedWords -> word.words.any { isCarrier(it) }

        is Word -> when (word.wordType) {
            WordType.FORMATIVE -> {
                val rootIndex = when {
                    word.getOrNull(0) in CC_CONSONANTS -> 2
                    word.getOrNull(0)?.isVowel() == true -> 1
                    else -> 0
                }

                if (word.getOrNull(rootIndex - 1) in SPECIAL_VV_VOWELS) return false

                word.getOrNull(rootIndex) == CARRIER_ROOT_CR
            }
            WordType.REFERENTIAL, WordType.COMBINATION_REFERENTIAL ->
                setOf("hl", "hn", "hň").any { it in word } //Quotative adjunct "hm" is not included

            else -> false
        }
    }

}