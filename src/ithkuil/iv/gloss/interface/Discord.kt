package ithkuil.iv.gloss.`interface`

import dev.kord.core.*
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent

import java.io.File
import java.lang.StringBuilder

import ithkuil.iv.gloss.logger

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
    if (user.isBot || !(content.startsWith("?") || content.contains(":?"))) return
    if (content == "?help") return sendHelp(user, channel)

    val replyTo = referencedMessage?.content

    val contentWithReply = if (replyTo != null && content matches "^\\S*$".toRegex()) {
        logger.info { "-> respond($content) replying to $replyTo" }
        "$content $replyTo"
    } else {
        logger.info { "-> respond($content)" }
        content
    }

    respond(contentWithReply)
        .also { logger.info { "   respond($content) -> ${"\n" + it}" } }
        ?.splitMessages()
        ?.forEach { channel.createMessage(it) }
}

suspend fun sendHelp(helpee: User, channel: MessageChannelBehavior) {
    logger.info { "-> sendHelp(${helpee.tag})" }
    val dmChannel = helpee.getDmChannelOrNull() ?: return
    val helpMessages = File("./resources/help.md")
        .readText()
        .splitMessages()

    helpMessages.forEach { dmChannel.createMessage(it) }

    channel.createMessage("Help sent your way, ${helpee.mention}!")
}

fun String.splitMessages(): Sequence<String> = sequence {
    val remainder = lineSequence().fold(StringBuilder()) { current, line ->
        if (current.length + line.length + 1 > 2000) {
            yield(current.toString())
            StringBuilder().appendLine(line)
        } else current.appendLine(line)
    }
    yieldAll(remainder.chunkedSequence(2000))
}
