package io.github.syst3ms.tnil

import dev.kord.core.*
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent

import java.io.File

suspend fun main() {
    val token = File("./resources/token.txt").readLines()[0]
    val kord = Kord(token)

    kord.on<MessageCreateEvent> {
        if (!message.content.startsWith("?") || message.author?.isBot != false) return@on

        if (message.content == "?help") {
            sendHelp(message)
            return@on
        }

        val response = respond(message.content) ?: return@on

        val messages = response.splitMessages()

        messages.forEach {
            message.channel.createMessage(it)
        }

    }

    loadResourcesOnline()
    kord.login {
        playing("?help for info")
    }
}

 suspend fun sendHelp(message : Message) {

    val helpee = message.author ?: return
    val dmChannel = helpee.getDmChannel()

    val helpMessages = File("./resources/help.md")
        .readText()
        .splitMessages()

    helpMessages.forEach { dmChannel.createMessage(it) }

    message.channel.createMessage("Help sent your way, ${helpee.mention}!")

}