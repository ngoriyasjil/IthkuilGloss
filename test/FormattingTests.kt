package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.Stress
import kotlin.test.Test

class FormattingTests {
    @Test
    fun stressTest() {
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
    fun stressErrorTest() {
        "á" givesInvalid "Marked default stress"
        "ála" givesInvalid "Marked default stress"
        "álá" givesInvalid "Double-marked stress"
        "álalala" givesInvalid "Unrecognized stress placement"
        "aí" givesInvalid "Unrecognized stress placement"
    }
}