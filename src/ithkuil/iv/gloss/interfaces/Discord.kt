package ithkuil.iv.gloss.interfaces

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.*
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.live.live
import dev.kord.core.live.on

import java.io.File
import java.lang.StringBuilder

import ithkuil.iv.gloss.dispatch.loadResourcesOnline
import ithkuil.iv.gloss.dispatch.logger
import ithkuil.iv.gloss.dispatch.respond as terminalRespond

import kotlinx.coroutines.delay

@KordPreview
suspend fun main() {
    val token = File("./resources/token.txt").readLines().first()
    val kord = Kord(token)
    kord.on<MessageCreateEvent> {
        replyAndTrackChanges()
    }

    kord.on<ReactionAddEvent> {

        val messag = message.asMessageOrNull() ?: return@on
        if (messag.author != kord.getSelf()) return@on
        if (user != messag.referencedMessage?.author) return@on
        if (emoji != ReactionEmoji.Unicode("\u274C")) return@on
        
        messag.delete()
    }

    initializeSlashCommands(kord)

    loadResourcesOnline()
    kord.login {
        playing("?help for info")
        logger.info { "Logged in!" }
    }
}

@KordPreview
private suspend fun MessageCreateEvent.replyAndTrackChanges() {
    val response = message.respondTo() ?: return

    val liveMessage = message.live()


    liveMessage.on<MessageUpdateEvent> {
        with(liveMessage.message) {
            val replyTo = message.referencedMessage?.content

            logger.debug { "replyTo: $replyTo" }

            val contentWithReply = if (replyTo != null && content matches "^\\S*$".toRegex()) {
                "$content $replyTo"
            } else {
                content
            }

            val editTo = terminalRespond(contentWithReply)?.splitMessages()?.first() ?: "*Unknown invocation*"

            logger.info { "Edited a message to $editTo responding to $contentWithReply" }

            response.edit {
                content = editTo
            }
        }
    }

    delay(60000)

    liveMessage.shutDown()
}

suspend fun Message.respondTo() : Message? {
    val user = author ?: return null
    if (user.isBot || !(content.startsWith("?") || content.contains(":?"))) return null
    if (content == "?help") {
        sendHelp(user, channel)
        return null
    }

    val replyTo = referencedMessage?.content

    val contentWithReply = if (replyTo != null && content matches "^\\S*$".toRegex()) {
        logger.info { "-> respond($content) replying to $replyTo" }
        "$content $replyTo"
    } else {
        logger.info { "-> respond($content)" }
        content
    }

    val replies = mutableListOf<Message>()

    terminalRespond(contentWithReply)
        .also { logger.info { "   respond($content) ->\n$it" } }
        ?.splitMessages()
        ?.forEach {
            val r = reply { content = it }
            replies.add(r)
        }

    return replies[0]
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
