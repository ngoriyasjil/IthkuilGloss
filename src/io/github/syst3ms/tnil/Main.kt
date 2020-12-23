package io.github.syst3ms.tnil

import dev.kord.core.*
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent

import java.io.File
import java.lang.StringBuilder

suspend fun main() {
    val token = File("./resources/token.txt").readLines()[0]
    val kord = Kord(token)

    kord.on<MessageCreateEvent> {
        with(message) {

            val author = author ?: return@on

            if (!content.startsWith("?") || !author.isBot) return@on

            if (content == "?help") {
                sendHelp(author, channel)
                return@on
            }

            val response = respond(content) ?: return@on
            val messages = response.splitMessages()

            messages.forEach { channel.createMessage(it) }
        }
    }

    loadResourcesOnline()
    kord.login {
        playing("?help for info")
    }
}

suspend fun sendHelp(helpee: User, channel : MessageChannelBehavior) {
     val dmChannel = helpee.getDmChannelOrNull() ?: return
     val helpMessages = File("./resources/help.md")
        .readText()
        .splitMessages()

    helpMessages.forEach { dmChannel.createMessage(it) }

    channel.createMessage("Help sent your way, ${helpee.mention}!")
}

fun String.splitMessages(): Sequence<String> = sequence {
    val remainder = lines().fold(StringBuilder()) { current, line ->
        if (current.length + line.length + 1 > 2000) {
            yield(current.toString())
            StringBuilder(line)
        } else current.appendLine(line)
    }
    yield(remainder.toString())
}