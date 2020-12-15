package io.github.syst3ms.tnil

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File

fun main() {
    val tokenFile = File("./resources/token.txt")
    require(tokenFile.exists() && tokenFile.isFile) { "Can't find token file!" }
    val token = tokenFile.readLines()[0]
    loadResourcesOnline()
    val jda = JDABuilder.createDefault(token)
        .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "?help for info"))
        .addEventListeners(MessageListener())
        .build()
    jda.awaitReady()
}

class MessageListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channelType != ChannelType.TEXT && event.channelType != ChannelType.PRIVATE)
            return
        val channel = event.channel
        val message = event.message
        val content = message.contentRaw
        if (!content.startsWith("?")) {
            return
        }
        if (event.channelType == ChannelType.TEXT && !event.textChannel.canTalk(event.guild.selfMember)) {
            println("Can't talk in channel #" + channel.name)
            return
        }

        if (content.startsWith("?help")) {
            if (sendHelp(event, channel)) {
                return
            }
        }

        val response = respond(content)
        if (response != null) {
            channel.sendMessage(MessageBuilder(response).build()).queue()
        }
    }

    private fun sendHelp(event: MessageReceivedEvent, chan: MessageChannel): Boolean {
        val helpMessage = File("./resources/help.md").readText().split("SPLITMESSAGEHERE")
        val first = MessageBuilder().append(helpMessage[0])
        val second = MessageBuilder().append(helpMessage[1])

        val helpee = event.author
        if (event.channelType == ChannelType.TEXT) {
            helpee.openPrivateChannel()
                .flatMap { it.sendMessage(first.build()) }
                .flatMap { it.channel.sendMessage(second.build()) }
                .queue({
                    chan.sendMessage("Help was sent your way, ${helpee.asMention}!").queue()
                }) { // Failure
                    val m = second.append("\n")
                        .append("(Couldn't send the message in DMs, ${helpee.asMention})")
                        .build()
                    chan.sendMessage(first.build())
                        .queue()
                    chan.sendMessage(m)
                        .queue()
                }
        } else {
            chan.sendMessage(first.build())
                .queue()
            return true
        }
        return false
    }
}
