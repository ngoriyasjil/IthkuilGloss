package ithkuil.iv.gloss.test

import ithkuil.iv.gloss.dispatch.respond
import ithkuil.iv.gloss.interfaces.splitMessages
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class DiscordTests {
    @Test
    fun longMessageTest() {
        val longText = File("./resources/longtest.txt").readText()
        val messages = respond("?gloss $longText")!!.splitMessages().toList()
        assertTrue("Is empty!") { messages.isNotEmpty() }
        assertTrue("Wrong size: ${messages.size}") { messages.size == 2 }
        assertTrue("Are longer than 2000 chars ${messages.map { it.length }}") { messages.all { it.length <= 2000 } }
    }
}