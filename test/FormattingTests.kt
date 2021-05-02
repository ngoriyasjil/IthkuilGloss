package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.Invalid
import ithkuil.iv.gloss.Stress
import ithkuil.iv.gloss.Word
import ithkuil.iv.gloss.formatWord
import kotlin.test.Test
import kotlin.test.assertEquals

infix fun String.hasStress(stress: Stress) = assertEquals(stress, (formatWord(this) as Word).stress, this)

infix fun String.givesInvalid(error: String) = assertEquals(error, (formatWord(this) as Invalid).message)

class FormattingTests {

    @Test
    fun `Stress examples`() {
        "a" hasStress Stress.MONOSYLLABIC
        "ala" hasStress Stress.PENULTIMATE
        "alá" hasStress Stress.ULTIMATE
        "lìala" hasStress Stress.PENULTIMATE
        "ua" hasStress Stress.PENULTIMATE
        "ëu" hasStress Stress.MONOSYLLABIC
        "alái" hasStress Stress.ULTIMATE
        "ái'la'sa" hasStress Stress.ANTEPENULTIMATE
        "ála'a" hasStress Stress.ANTEPENULTIMATE
    }

    @Test
    fun `Stress error examples`() {
        "á" givesInvalid "Marked default stress"
        "ála" givesInvalid "Marked default stress"
        "álá" givesInvalid "Double-marked stress"
        "álalala" givesInvalid "Unrecognized stress placement"
        "aí" givesInvalid "Unrecognized stress placement"
    }

    @Test
    fun `Sentence start prefix formatting errors`() {
        "çêlala" givesInvalid "Stress on sentence prefix"
        "ç" givesInvalid "Lone sentence prefix"
        "çw" givesInvalid "Lone sentence prefix"
        "çç" givesInvalid "Lone sentence prefix"
    }
}