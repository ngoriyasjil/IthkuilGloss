package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.Valid
import ithkuil.iv.gloss.formatWord
import ithkuil.iv.gloss.isCarrier
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContextTests {

    @Test
    fun `Carrier identification examples`() {
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

fun assertCarrier(word: String) {
    assertTrue(word) { isCarrier(formatWord(word) as Valid) }
}

fun assertNotCarrier(word: String) {
    assertFalse(word) { isCarrier(formatWord(word) as Valid) }
}