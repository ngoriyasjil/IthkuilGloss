package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

infix fun String.glossesTo(gloss: String) {

    val parse = when (val word = formatWord(this)) {
        is Word -> parseWord(word)
        is ConcatenatedWords -> parseConcatenationChain(word)
        is Invalid -> throw AssertionError(word.message)
    }

    val (result, message) = when (parse) {
        is Error -> null to "Error: ${parse.message}"
        is Foreign -> null to "Foreign: ${parse.word}"
        is Gloss -> parse.toString(GlossOptions()) to this
    }

    assertEquals(gloss, result, message)
}

infix fun String.givesError(error: String) {

    val parse = when (val word = formatWord(this)) {
        is Word -> parseWord(word)
        is ConcatenatedWords -> parseConcatenationChain(word)
        is Invalid -> throw AssertionError(word.message)
    }

    val (result, message) = when (parse) {
        is Error -> parse.message to this
        is Foreign -> null to "Foreign: ${parse.word}"
        is Gloss -> null to parse.toString(GlossOptions())
    }

    assertEquals(error, result, message)
}

infix fun String.hasStress(stress: Stress) = assertEquals(stress, (formatWord(this) as Word).stress, this)

infix fun String.givesInvalid(error: String) = assertEquals(error, (formatWord(this) as Invalid).message)

infix fun String.isCaOf(gloss: String) = assertEquals(gloss, parseCa(this)?.toString(GlossOptions()), this)

fun assertCarrier(word: String) {
    assertTrue(word) { isCarrier(formatWord(word) as Valid) }
}

fun assertNotCarrier(word: String) {
    assertFalse(word) { isCarrier(formatWord(word) as Valid) }
}

