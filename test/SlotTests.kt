package ithkuil.iv.gloss.test

import kotlin.test.Test

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