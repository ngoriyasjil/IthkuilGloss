import kotlin.test.Test
import kotlin.test.assertEquals
import ithkuil.iv.gloss.*
import java.io.File
import kotlin.test.assertTrue

infix fun String.glossesTo(gloss: String) {

  val (result, message) = when (val parse = parseWord(this)) {
    is Error -> null to "Error: ${parse.message}"
    is Gloss -> parse.toString(GlossOptions(Precision.REGULAR)) to this
  }

  assertEquals(gloss, result, message)
}

infix fun String.givesError(error: String) {

  val (result, message) = when (val parse = parseWord(this)) {
    is Error -> parse.message to this
    is Gloss -> null to parse.toString(GlossOptions(Precision.REGULAR))
  }

  assertEquals(error, result, message)
}

infix fun String.hasStress(stress: Int) = assertEquals(stress, splitGroups().findStress(), this)

infix fun String.mustBe(s: String) = assertEquals(s, this, this)

class GeneralTests {

  @Test
  fun poemTest() {
    "hlamröé-uçtļořï" glossesTo "T1-S1-**mr**-PCR—S3-**çtļ**-DYN/CSV-RPV-STM"
    "khe" glossesTo  "Obv/DET-ABS"
    "adnilö'ö" glossesTo  "S1-**dn**-OBJ-UTL"
    "yeilaiceu" glossesTo  "S2/RPV-**l**-**c**/1₂-ATT"
    "aiňļavu'u" glossesTo  "S1/**r**/4-**ňļ**-N-RLT"
  }

  @Test
  fun slotVTest() {
    "alarfull" glossesTo "S1-**l**-**rf**/9₁-{Ca}"
    "wa'lena" givesError "Unexpectedly few slot V affixes"
  }

  @Test
  fun stressTest() {
    "a" hasStress -1
    "ala" hasStress 1
    "alá" hasStress 0
    "lìala" hasStress 1
    "ua" hasStress 1
    "ëu" hasStress -1
    "alái" hasStress 0
    "ála'a" hasStress 2
  }

  @Test
  fun wordTypeTest() {
    "muyüs" glossesTo "ma-IND-DAT-2m"
  }

  @Test
  fun affixualAdjunctTest() {
    "ïn" glossesTo "**n**/4₁"
    "ïní" glossesTo "**n**/4₁-{VIISub}-{concat.}"
  }

  @Test
  fun caUnGeminationTest() {
    "pp".unGeminateCa() mustBe "p"
    "ggw".unGeminateCa() mustBe "gw"
    "mmtw".unGeminateCa() mustBe "mtw"
    "tççkl".unGeminateCa() mustBe "tçkl"
    "ccw".unGeminateCa() mustBe "cw"
    "ččtw".unGeminateCa() mustBe "čtw"
    "gd".unGeminateCa() mustBe "kt"
    "žžn".unGeminateCa() mustBe "dn"
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
    "öëgüöl" glossesTo "CPT/DYN-**g**/0-D0/OBJ"
  }

  @Test
  fun glottalShiftTest() {
    "lala'a" glossesTo "S1-**l**-PRN"
    "la'la" glossesTo "S1-**l**-PRN"
    "wala'ana" glossesTo "S1-**l**-**n**/1₁-{Ca}"
    "halala'a" givesError "Unexpected glottal stop in incorporated formative"
    "a'lananalla'a" glossesTo "S1-**l**-**n**/1₁-**n**/1₁-{Ca}-PRN"
    "a'la'nanalla" glossesTo "S1-**l**-**n**/1₁-**n**/1₁-{Ca}-PRN"
    "a'la'nanalla'a" givesError "Too many glottal stops found"
  }

  @Test
  fun caTest() {
    "alartřa" glossesTo "S1-**l**-DSS/RPV"
  }

  @Test
  fun longMessageTest() {
    val longText = File("./resources/longtest.txt").readText()
    val messages = respond("?gloss $longText")!!.splitMessages().toList()
    assertTrue("Is empty!") { messages.isNotEmpty() }
    assertTrue("Wrong size: ${messages.size}") { messages.size == 2 }
    assertTrue("Are longer than 2000 chars ${messages.map { it.length }}") { messages.all { it.length <= 2000 } }

  }


}
