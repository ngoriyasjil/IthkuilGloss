package io.github.syst3ms.tnil

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.ChannelType
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
        val chan = event.channel
        val msg = event.message
        val content = msg.contentRaw
        if (!content.startsWith("?")) {
            return
        }
        if (event.channelType == ChannelType.TEXT && !event.textChannel.canTalk(event.guild.selfMember)) {
            println("Can't talk in channel #" + chan.name)
            return
        }

        if (content.startsWith("?help")) {
            val helpMessage = File("./resources/help.md").readText().split("SPLITMESSAGEHERE")
            val newMessage = MessageBuilder()
                    .append(helpMessage[0])
            val second = MessageBuilder()
                    .append(helpMessage[1])
            val auth = event.author
            if (event.channelType == ChannelType.TEXT) {
                auth.openPrivateChannel()
                        .flatMap { it.sendMessage(newMessage.build()) }
                        .flatMap { it.channel.sendMessage(second.build()) }
                        .queue({
                            chan.sendMessage("Help was sent your way, " + auth.asMention + "!").queue()
                        }) { // Failure
                            val m = second.append("\n")
                                    .append("(Couldn't send the message in DMs, ${auth.asMention})")
                                    .build()
                            chan.sendMessage(newMessage.build())
                                    .queue()
                            chan.sendMessage(m)
                                    .queue()
                        }
            } else {
                chan.sendMessage(newMessage.build())
                        .queue()
                return
            }
        }

        val response = respond(content)
        if (response != null) {
            chan.sendMessage(MessageBuilder(response).build()).queue()
        }
    }
}
