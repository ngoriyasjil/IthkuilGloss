package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.*
import kotlin.test.*

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
        parseVv("a", null) hasGlossOf "S1"
        parseVv("ö", null) hasGlossOf "S0.CPT"
        parseVv("ua", null) hasGlossOf "S3.**t**/4"
        parseVv("oë", null) hasGlossOf "CPT.DYN"
        parseVv("ae", null) hasGlossOf ""
    }

    @Test
    fun `Vv examples with shortcuts`() {
        parseVv("io", Shortcut.W) hasGlossOf "S2.N"
        parseVv("io", Shortcut.Y) hasGlossOf "S2.A"
        parseVv("ae", Shortcut.Y) hasGlossOf "PRX"
    }

    @Test
    fun `Normal Vr examples`() {
        parseVr("ä") hasGlossOf "CTE"
        parseVr("o") hasGlossOf "DYN.CSV"
        parseVr("öe") hasGlossOf "DYN.OBJ.AMG"
    }

    @Test
    fun `Affix Vr examples`() {
        parseAffixVr("a") hasGlossOf "D1"
        parseAffixVr("ai") hasGlossOf "D1.FNC"
        parseAffixVr("ae") hasGlossOf "D0"
        parseAffixVr("ea") hasGlossOf "D0.FNC"
        parseAffixVr("üo") hasGlossOf "D0.RPS"
        parseAffixVr("üö") hasGlossOf "D0.AMG"
    }

    @Test
    fun `Ca examples`() {
        parseCa("l") hasGlossOf ""
        parseCa("s") hasGlossOf "DPX"
        parseCa("nļ") hasGlossOf "ASO"
        parseCa("tļ") hasGlossOf "RPV"
        parseCa("řktç") hasGlossOf "VAR.MSC.PRX.A.RPV"
        parseCa("nš") hasGlossOf "COA.G.RPV"
        parseCa("zḑ") hasGlossOf "MFS.DPL.A.RPV"
        parseCa("nx") hasGlossOf "MSC.GRA.N.RPV"
    }

    @Test
    fun `VnCn parses for all values in CN_CONSONANTS`() {
        CN_CONSONANTS.forEach { cn ->
            assertNotNull(parseVnCn("a", cn))
        }
    }

    @Test
    fun `VnCn examples`() {
        parseVnCn("i", "h") hasGlossOf "RCP"
        parseVnCn("i", "w") hasGlossOf "PRG"
        parseVnCn("ai", "hl") hasGlossOf "PCT.SUB"
        parseVnCn("ia", "hl", marksMood = false) hasGlossOf "1:BEN.CCA"
        parseVnCn("ao", "h") hasGlossOf "MIN"
        parseVnCn("ao", "hny") hasGlossOf "DCL.HYP"
        parseVnCn("ao", "h", absoluteLevel = true) hasGlossOf "MINa"
    }

    @Test
    fun `Absolute Level fails with other vowel series`() {
        assertNull(parseVnCn("a", "h", absoluteLevel = true))
        assertNull(parseVnCn("ai", "h", absoluteLevel = true))
        assertNull(parseVnCn("ia", "h", absoluteLevel = true))
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

infix fun Glossable?.hasGlossOf(expected: String) {
    val gloss = this?.gloss(GlossOptions())
    assertEquals(expected, gloss)
}