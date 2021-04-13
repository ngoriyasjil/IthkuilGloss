package ithkuil.iv.gloss.test

import kotlin.test.Test

class WordTests {

    @Test
    fun `Slot V marking matches actual number of slot V affixes`() {
        "alarfull" glossesTo "S1-**l**-**rf**/9₁-{Ca}"
        "a'larfunall" glossesTo "S1-**l**-**rf**/9₁-**n**/1₁-{Ca}"
        "wa'lena" givesError "Unexpectedly few slot V affixes"
        "waršana'anera" givesError "Unexpectedly many slot V affixes"
    }

    @Test
    fun `The Vc glottal stop can be moved`() {
        "lala'a" glossesTo "S1-**l**-PRN"
        "la'la" glossesTo "S1-**l**-PRN"
        "wala'ana" glossesTo "S1-**l**-**n**/1₁-{Ca}"
        "halala'a-alal" givesError "Unexpected glottal stop in concatenated formative"
        "a'lananalla'a" glossesTo "S1-**l**-**n**/1₁-**n**/1₁-{Ca}-PRN"
        "a'la'nanalla" glossesTo "S1-**l**-**n**/1₁-**n**/1₁-{Ca}-PRN"
        "a'la'nanalla'a" givesError "Too many glottal stops found"
    }

    @Test
    fun wordTypeTest() {
        "muyüs" glossesTo "ma-IND-DAT-2m"
    }

    @Test
    fun csRootTest() {
        "oëgöil" glossesTo "CPT.DYN-**g**/0-D0.OBJ"
    }

    @Test
    fun stressMarkingTest() {
        "lála'a" glossesTo "S1-**l**-PRN\\FRA"
        "layá" glossesTo "1m-THM-THM\\RPV"
    }
}

