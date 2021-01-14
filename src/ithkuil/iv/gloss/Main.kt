package ithkuil.iv.gloss

import dev.kord.core.*
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent

import java.io.File
import java.lang.StringBuilder

import mu.KotlinLogging

val logger = KotlinLogging.logger { }

suspend fun main() {
    val token = File("./resources/token.txt").readLines()[0]
    val kord = Kord(token)
    kord.on<MessageCreateEvent> { message.respondTo() }

    loadResourcesOnline()
    kord.login {
        playing("?help for info")
        logger.info { "Logged in!" }
    }
}

suspend fun Message.respondTo() {
    val user = author ?: return
    if (user.isBot || !content.startsWith("?")) return
    if (content == "?help") return sendHelp(user, channel)

    logger.info { "-> respond($content)" }
    respond(content)
        .also { logger.info { "   respond($content) -> ${"\n" + it}" } }
        ?.splitMessages()
        ?.forEach { channel.createMessage(it) }
}

suspend fun sendHelp(helpee: User, channel: MessageChannelBehavior) {
    logger.info { "-> sendHelp($helpee)" }
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
            StringBuilder().appendLine(line)
        } else current.appendLine(line)
    }
    yieldAll(remainder.chunkedSequence(2000))
}
