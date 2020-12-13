import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.syst3ms.tnil.*

infix fun String.glossesTo(gloss: String) {

  val (result, message) = when (val parse = parseWord(this)) {
    is Error -> null to "Error: ${parse.message}"
    is Gloss -> parse.toString(1, true) to this
  }

  assertEquals(gloss, result, message)
}

infix fun String.givesError(error: String) {

  val (result, message) = when (val parse = parseWord(this)) {
    is Error -> parse.message to this
    is Gloss -> null to parse.toString(1, true)
  }

  assertEquals(error, result, message)
}

infix fun String.hasStress(stress: Int) = assertEquals(stress, splitGroups().findStress(), this)

infix fun String.mustBe(s: String) = assertEquals(s, this, this)

class TestTest {

  @Test
  fun poemTest() {
    "hlamröé-uçtļořë" glossesTo "S1-**mr**-PCR—S3-**çtļ**-DYN/CSV-RPV-STM"
    "khe" glossesTo  "Obv/DET-ABS"
    "adnilo'o" glossesTo  "S1-**dn**-OBJ-UTL"
    "yeilaišeu" glossesTo  "S2/RPV-**l**-**š**/1₂-ATT"
    "aiňļavu'u" glossesTo  "S1/**r**/4-**ňļ**-N-RLT"
  }

  @Test
  fun slotVTest() {
    "alarfull" glossesTo "S1-**l**-**rf**/9₁"
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
    "hën" glossesTo "**n**/4₁"
    "hëní" glossesTo "**n**/4₁-{VIISub}-{concat.}"
  }

  @Test
  fun caUnGeminationTest() {
    "pp".unGeminateCa() mustBe "p"
    "ggw".unGeminateCa() mustBe "gw"
    "mmtw".unGeminateCa() mustBe "mtw"
    "tççkl".unGeminateCa() mustBe "tçkl"
    "ẓw".unGeminateCa() mustBe "cw"
    "jtw".unGeminateCa() mustBe "čtw"
    "gd".unGeminateCa() mustBe "kt"
    "jn".unGeminateCa() mustBe "dn"
  }

  @Test
  fun vnCnTest() {
    "auha" glossesTo "PCT"
    "aiha" glossesTo "RCP"
    "ëha" givesError "Unknown VnCn: ëh"
  }

  @Test
  fun csRootTest() {
    "öëgüöl" glossesTo "CPT/DYN-**g**/0-D0/OBJ"
  }

}
