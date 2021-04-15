package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class SlotTests {

    @Test
    fun `Cc parses only for values in CC_CONSONANTS`() {
        CC_CONSONANTS.forEach { it isNotCcOf (null to null) }
        (CN_CONSONANTS - CC_CONSONANTS).forEach { it isCcOf (null to null) }
    }

    @Test
    fun ccExamples() {
        "hl" isCcOf (Concatenation.TYPE_ONE to Shortcut.W_SHORTCUT)
        "y" isCcOf (null to Shortcut.Y_SHORTCUT)
        "hw" isCcOf (Concatenation.TYPE_TWO to null)
    }

    @Test
    fun `Special Vv parses for all values in SPECIAL_VV_VOWELS`() {
        SPECIAL_VV_VOWELS.forEach {
            assertNotNull(parseSpecialVv(it, null), it)
        }
    }

    @Test
    fun caExamples() {
        "l" isCaOf ""
        "s" isCaOf "DPX"
        "nļ" isCaOf "ASO"
        "tļ" isCaOf "RPV"
        "řktç" isCaOf "VAR.MSC.PRX.A.RPV"
        "nš" isCaOf "COA.G.RPV"
        "zḑ" isCaOf "MFS.DPL.A.RPV"
        "nx" isCaOf "MSC.GRA.N.RPV"
    }
}

infix fun String.isCcOf(pair: Pair<Concatenation?, Shortcut?>) {
    val gloss = parseCc(this)
    assertEquals(pair, gloss, this)
}

infix fun String.isNotCcOf(illegal: Pair<Concatenation?, Shortcut?>) {
    val gloss = parseCc(this)
    assertNotEquals(illegal, gloss, this)
}

infix fun String.isCaOf(expected: String) {
    val gloss = parseCa(this)?.toString(GlossOptions())
    assertEquals(expected, gloss, this)
}