import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.syst3ms.tnil.*

infix fun String.glossesTo(gloss: String) = assertEquals(gloss, parseWord(this, 1, true), this)

infix fun String.hasStress(stress: Int) = assertEquals(stress, splitGroups().findStress(), this)

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
    "lìala" hasStress 2
    "ua" hasStress 1
    "ëu" hasStress -1
    "alái" hasStress 0
    "ála'a" hasStress 2
  }

  @Test
  fun wordTypeTest() {
    "muyüs" glossesTo "ma-IND-DAT-2m"
  }

}
