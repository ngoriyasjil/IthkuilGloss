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
    fun `Cc examples`() {
        "hl" isCcOf (Concatenation.TYPE_ONE to Shortcut.W)
        "y" isCcOf (null to Shortcut.Y)
        "hw" isCcOf (Concatenation.TYPE_TWO to null)
    }

    @Test
    fun `Special Vv parses for all values in SPECIAL_VV_VOWELS`() {
        SPECIAL_VV_VOWELS.forEach {
            assertNotNull(parseSpecialVv(it, null), it)
        }
    }

    @Test
    fun `Vv examples with no shortcut`() {
        "a" isVvOf "S1.PRC"
        "ö" isVvOf "S0.CPT"
        "ua" isVvOf "S3.PRC.**t**/4"
        "oë" isVvOf "CPT.DYN"
        "ae" isVvOf "PRC"
    }

    @Test
    fun `Vv examples with shortcuts`() {
        parseVv("io", Shortcut.W) hasGlossOf "S2.N"
        parseVv("io", Shortcut.Y) hasGlossOf "S2.A"
        parseVv("ae", Shortcut.Y) hasGlossOf "PRX"
    }

    @Test
    fun `Cs root Vvs return an error with shortcuts`() {

    }

    @Test
    fun `Ca examples`() {
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
    val gloss = parseCa(this)?.gloss(GlossOptions())
    assertEquals(expected, gloss, this)
}

infix fun String.isVvOf(expected: String) {
    val gloss = parseVv(this, shortcut = null)
        ?.gloss(GlossOptions(includeDefaults = true))
    assertEquals(expected, gloss, this)
}

infix fun Glossable?.hasGlossOf(expected: String) {
    val gloss = this?.gloss(GlossOptions())
    assertEquals(expected, gloss)
}