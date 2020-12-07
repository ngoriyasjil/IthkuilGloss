import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.syst3ms.tnil.*

fun Map<String, String>.glossTest(precision: Int = 1, ignoreDefault: Boolean = true) =
  mapKeys { (word,_) -> parseWord(word, precision, ignoreDefault) }
  .forEach { (gloss, expected) -> assertEquals(expected, gloss) }

class TestTest {

  @Test
  fun poemTest() {
    val glosses = mapOf(
      "hlamröé-uçtļořë" to "S1-**mr**-PCR—S3-**çtļ**-DYN/CSV-RPV-STM",
      "khe" to "Obv/DET-ABS",
      "adnilo'o" to "S1-**dn**-OBJ-UTL",
      "yeilaišeu" to "S2/RPV-**l**-**š**/1₂-ATT",
      "aiňļavu'u" to "S1/**r**/4-**ňļ**-N-RLT"
    )

    glosses.glossTest()
  }

  @Test
  fun stressBiasTest() {
    mapOf("čpwälahái'ļļč" to "S1-**čpw**-CTE-RSP/OBS-STU").glossTest()
  }

  @Test
  fun slotVTest() {
    mapOf("alarfu'ul" to "S1-**l**-**rf**/9₁").glossTest()
  }

  @Test
  fun stressTest() {
    assertEquals(-1, "a".splitGroups().findStress())
    assertEquals(1, "ala".splitGroups().findStress())
    assertEquals(0, "alá".splitGroups().findStress())
    assertEquals(1, "lìala".splitGroups().findStress(), "lìala")
    assertEquals(1, "ua".splitGroups().findStress(), "ua")
    assertEquals(-1, "ëu".splitGroups().findStress())
    assertEquals(0, "alái".splitGroups().findStress())
  }

}
