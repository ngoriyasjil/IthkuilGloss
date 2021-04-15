package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.GlossOptions
import ithkuil.iv.gloss.parseCa
import kotlin.test.Test
import kotlin.test.assertEquals

infix fun String.isCaOf(gloss: String) = assertEquals(gloss, parseCa(this)?.toString(GlossOptions()), this)

class SlotTests {
    @Test
    fun caTest() {
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