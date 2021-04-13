package ithkuil.iv.gloss.test

import kotlin.test.Test

class ContextTests {

    @Test
    fun carrierIdentificationTest() {
        assertCarrier("sala")
        assertCarrier("husana-mala")
        assertCarrier("hamala-sala")
        assertCarrier("hla")
        assertCarrier("hňayazë")
        assertCarrier("ahnaxena")
        assertNotCarrier("hma")
        assertNotCarrier("ëisala")
    }

}