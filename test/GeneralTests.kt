import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ithkuil.iv.gloss.*
import ithkuil.iv.gloss.dispatch.respond
import ithkuil.iv.gloss.interfaces.splitMessages
import java.io.File
import kotlin.test.assertFalse

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

infix fun String.mustBe(s: String) = assertEquals(s, this, this)

fun assertCarrier(word: String) {
    assertTrue(word) { isCarrier(formatWord(word) as Valid) }
}

fun assertNotCarrier(word: String) {
    assertFalse(word) { isCarrier(formatWord(word) as Valid) }
}

class GeneralTests {

    @Test
    fun poemTest() {
        "hlamröé-uçtļořï" glossesTo "T1-S1-**mr**-PCR—S3-**çtļ**-DYN.CSV-RPV-STM"
        "khe" glossesTo "Obv.DET-ABS"
        "adnilö'ö" glossesTo "S1-**dn**-OBJ-UTL"
        "yeilaišeu" glossesTo "S2.RPV-**l**-**š**/1₂-ATT"
        "aiňļavu'u" glossesTo "S1.**r**/4-**ňļ**-N-RLT"
    }

    @Test
    fun slotVTest() {
        "alarfull" glossesTo "S1-**l**-**rf**/9₁-{Ca}"
        "wa'lena" givesError "Unexpectedly few slot V affixes"
        "waršana'anera" givesError "Unexpectedly many slot V affixes"
    }

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

    @Test
    fun wordTypeTest() {
        "muyüs" glossesTo "ma-IND-DAT-2m"
    }

    @Test
    fun affixualAdjunctTest() {
        "ïn" glossesTo "**n**/4₁"
        "ïní" glossesTo "**n**/4₁-{VIISub}\\{concat.}"
    }

    @Test
    fun caUnGeminationTest() {
        "pp".degeminateCa() mustBe "p"
        "ggw".degeminateCa() mustBe "gw"
        "mmtw".degeminateCa() mustBe "mtw"
        "tççkl".degeminateCa() mustBe "tçkl"
        "ccw".degeminateCa() mustBe "cw"
        "ččtw".degeminateCa() mustBe "čtw"
        "gḑḑ".degeminateCa() mustBe "kt"
        "žžn".degeminateCa() mustBe "dn"
    }

    @Test
    fun vnCnTest() {
        "iuha" glossesTo "VAC"
        "ïha" glossesTo "RCP"
        "ëuha" glossesTo "UNK"
        "ëha" givesError "Unknown VnCn: ëh"
    }

    @Test
    fun csRootTest() {
        "öëgüöl" glossesTo "CPT.DYN-**g**/0-D0.OBJ"
    }

    @Test
    fun glottalShiftTest() {
        "lala'a" glossesTo "S1-**l**-PRN"
        "la'la" glossesTo "S1-**l**-PRN"
        "wala'ana" glossesTo "S1-**l**-**n**/1₁-{Ca}"
        "halala'a-alal" givesError "Unexpected glottal stop in concatenated formative"
        "a'lananalla'a" glossesTo "S1-**l**-**n**/1₁-**n**/1₁-{Ca}-PRN"
        "a'la'nanalla" glossesTo "S1-**l**-**n**/1₁-**n**/1₁-{Ca}-PRN"
        "a'la'nanalla'a" givesError "Too many glottal stops found"
    }

    @Test
    fun caTest() {
        "alartřa" glossesTo "S1-**l**-DSS.RPV"
    }

    @Test
    fun longMessageTest() {
        val longText = File("./resources/longtest.txt").readText()
        val messages = respond("?gloss $longText")!!.splitMessages().toList()
        assertTrue("Is empty!") { messages.isNotEmpty() }
        assertTrue("Wrong size: ${messages.size}") { messages.size == 2 }
        assertTrue("Are longer than 2000 chars ${messages.map { it.length }}") { messages.all { it.length <= 2000 } }
    }

    @Test
    fun stressMarkingTest() {
        "lála'a" glossesTo "S1-**l**-PRN\\FRA"
        "layá" glossesTo "1m-THM-THM\\RPV"
    }

    @Test
    fun suppletiveFormsTest() {
        "hňa'u" glossesTo "[PHR]-ASI"
        "ïhlarxal" glossesTo "[CAR]-OBJ-**l**/1₁"
        "ëhňarxal" givesError "Epenthetic ï must only be used with Suppletive forms"
        "lëhmoyehnë" glossesTo "[1m+[QUO]]-ERG-ABS-[NAM]"
    }

    @Test
    fun carrierIdentificationTest() {
        assertCarrier("sala")
        assertCarrier("husana-mala")
        assertCarrier("hamala-sala")
        assertCarrier("hla")
        assertCarrier("hňayazë")
        assertCarrier("ïhnaxena")
        assertNotCarrier("hma")
        assertNotCarrier("ëisala")
    }


}
