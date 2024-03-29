package ithkuil.iv.gloss.interfaces

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
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
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.request.RestRequestException
import ithkuil.iv.gloss.dispatch.loadResourcesOnline
import ithkuil.iv.gloss.dispatch.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import ithkuil.iv.gloss.dispatch.respond as terminalRespond

@OptIn(PrivilegedIntent::class)
@KordPreview
suspend fun main() {
    val token = File("./resources/token.txt").readLines().first()
    val kord = Kord(token)
    kord.on<MessageCreateEvent> {
        logger.debug { "Saw a message: \"${message.content}\"" }
        replyAndTrackChanges()
    }

    kord.on<ReactionAddEvent> {

        val messag = message.asMessageOrNull() ?: return@on
        if (messag.author != kord.getSelf()) return@on
        if (emoji != ReactionEmoji.Unicode("\u274C")) return@on
        if (messag.referencedMessage != null && user != messag.referencedMessage?.author) return@on

        messag.delete()
    }

    loadResourcesOnline()
    kord.login {
        intents {
            +Intent.MessageContent
            +Intent.GuildMessages
            +Intent.GuildMessageReactions
            +Intent.DirectMessages
            +Intent.DirectMessagesReactions
        }
        presence { playing("?help for info") }
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

    liveMessage.coroutineContext.job.invokeOnCompletion {
        if ((it as? CancellationException)?.message == "Minute passed") return@invokeOnCompletion
        logger.debug { "Original message deleted" }
        runBlocking { response.delete("Original message deleted") }
    }

    delay(60000)

    liveMessage.shutDown(CancellationException("Minute passed"))
}

suspend fun Message.respondTo(): Message? {
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

    val helpMessages = File("./resources/help.md")
        .readText()
        .splitMessages()

    try {
        val dmChannel = helpee.getDmChannel()
        helpMessages.forEach { dmChannel.createMessage(it) }
    } catch (e: RestRequestException) {
        channel.createMessage(
            "Couldn't send help your way, ${helpee.mention}! " +
                "Your DMs might be disabled for this server."
        )
        return
    }


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
