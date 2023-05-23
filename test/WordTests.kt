package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.*
import kotlin.test.Test
import kotlin.test.assertEquals

class WordTests {

    @Test
    fun `Poem test`() {
        "yužgrá" glossesTo "S3.PRX-**žgr**-OBS"
        "eolaleici" glossesTo "S2.**t**/5-**l**-**c**/3₂-AFF"
        "hlamröé-úçtļořëi" glossesTo "T1-S1-**mr**-PCR—S3-**çtļ**-DYN.CSV-G.RPV-STM\\FRA"
        "khe" glossesTo "Rdp.DET-ABS"
        "adni'lö" glossesTo "S1-**dn**-OBJ-UTL"
        "yeilaiceu" glossesTo "S2.RPV-**l**-**c**/1₂-ATT"
        "aiňļa'vu" glossesTo "S1.**r**/4-**ňļ**-N-RLT"
    }

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
        "a'la'nanalla'a" givesError "Too many glottal stops"
    }

    @Test
    fun `Cs root formative examples`() {
        "ëilal" glossesTo "**l**/1-D1"
        "oërmölá" glossesTo "CPT.DYN-**rm**/6-D6-OBS"
        "oërmoulá" glossesTo "CPT.DYN-**rm**/6-D6.FNC-OBS"
    }

    @Test
    fun `Cs root Vvs return an error with shortcuts`() {
        "wëil" givesError "Shortcuts can't be used with a Cs-root"
    }

    @Test
    fun `One example of each word type parses correctly`() {
        "lalu" glossesTo "S1-**l**-IND"
        "ihnú" glossesTo "RCP.COU-{under adj.}"
        "äst" glossesTo "**st**/2₁"
        "miyüs" glossesTo "ma-AFF-DAT-2m"
        "mixenüa" glossesTo "ma-AFF-**n**/3₁-THM"
        "ha" glossesTo "DSV"
        "pļļ" glossesTo "“Funny!“"
        "hrei" glossesTo "CCA"
    }


    @Test
    fun `Stress-marked categories are glossed with a slash`() {
        "lála'a" glossesTo "S1-**l**-PRN\\FRA"
        "layá" glossesTo "1m-THM-THM\\RPV"
    }

    @Test
    fun `Sentence prefix examples`() {
        "çëlal" glossesTo "[sentence:]-S1-**l**"
        "çalal" glossesTo "[sentence:]-S1-**l**"
        "çwala" glossesTo "[sentence:]-S1-**l**"
        "ççala" glossesTo "[sentence:]-S1.PRX-**l**"
        "çëhamala-lala" glossesTo "[sentence:]-T1-S1-**m**—S1-**l**"
        "hamala-çëlala" givesError "Sentence prefix inside concatenation chain"
    }

    @Test
    fun `Combination referentials are distinguished from similar-looking formatives`() {
        "ţnaxekka" glossesTo "S1-**ţn**-**x**/3₁-MSC"
        "ţnaxeka" glossesTo "[mi.BEN+2p]-**k**/3₁"
    }
}

infix fun String.glossesTo(gloss: String) {

    val parse = when (val word = formatWord(this)) {
        is Word -> parseWord(word)
        is ConcatenatedWords -> parseConcatenationChain(word)
        is Invalid -> throw AssertionError(word.message)
    }

    val (result, message) = when (parse) {
        is Error -> null to "Error: ${parse.message}"
        is Parsed -> parse.gloss(GlossOptions()) to this
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
        is Parsed -> null to parse.gloss(GlossOptions())
    }

    assertEquals(error, result, message)
}