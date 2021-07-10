package ithkuil.iv.gloss.interfaces

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.entity.interaction.boolean
import dev.kord.core.entity.interaction.string
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import ithkuil.iv.gloss.dispatch.logger
import ithkuil.iv.gloss.dispatch.respond


@KordPreview
suspend fun initializeSlashCommands(kord: Kord) {

    kord.slashCommands.createGlobalApplicationCommands {

        command("gloss", "Gloss Ithkuil text word by word") {
            string("words", "The Ithkuil text to gloss") {
                required = true
            }

            boolean("show-defaults", "Show default category values. Hidden by default") {
                required = false
            }

            string("precision", "How verbose the gloss should be") {
                required = false
                choice("full", "Full names of all categories are shown")
                choice("regular", "Non-affix categories are abbreviated")
                choice("short", "All categories are abbreviated")
            }
        }

        command("sgloss", "Gloss Ithkuil text linearly") {
            string("words", "The Ithkuil text to gloss") {
                required = true
            }

            boolean("show-defaults", "Show default category values. Hidden by default") {
                required = false
            }

            string("precision", "How verbose the gloss should be") {
                required = false
                choice("full", "Full names of all categories are shown")
                choice("regular", "Non-affix categories are abbreviated")
                choice("short", "All categories are abbreviated")
            }
        }

        command("root", "Get the descriptions of the stems of given roots") {
            string("crs", "The consonant forms of the roots") { required = true }
        }

        command("affix", "Get the descriptions of the degrees of given affixes") {
            string("cxs", "The consonant forms of the affixes") { required = true }
        }

        command("ej", "Check a text for External Juncture violations") {
            string("text", "The text to be checked") { required = true }
        }

        command("whosacutebot", "Tells the bot how cute a bot it is. :3") { }

        command("date", "Tells the current time and date (UTC) in Ithkuil") { }

    }

    kord.on<InteractionCreateEvent> {

        val command = if (interaction is CommandInteraction) {
            (interaction as CommandInteraction).command
        } else return@on

        if (command.rootName != "gloss") return@on

        val showDefaults = command.options["show-defaults"]?.boolean() ?: false
        val precision = command.options["precision"]?.string() ?: "regular"

        val terminalCommand = constructCommand(showDefaults, precision)

        interaction.respondPublic {
            content = command.makeResponse("words") { words -> "$terminalCommand $words" }
        }
    }

    kord.on<InteractionCreateEvent> {

        val command = (interaction as? CommandInteraction)?.command ?: return@on

        if (command.rootName != "sgloss") return@on

        val showDefaults = command.options["show-defaults"]?.boolean() ?: false
        val precision = command.options["precision"]?.string() ?: "regular"

        val terminalCommand = constructCommand(showDefaults, precision, sgloss = true)

        interaction.respondPublic {
            content = command.makeResponse("words") { words -> "$terminalCommand $words" }
        }
    }

    kord.on<InteractionCreateEvent> {

        val command = (interaction as? CommandInteraction)?.command ?: return@on

        if (command.rootName != "root") return@on
        logger.info { "Running slash command \"root\"" }

        interaction.respondPublic {
            content = command.makeResponse("crs") { crs -> "?root $crs" }
        }
    }

    kord.on<InteractionCreateEvent> {

        val command = (interaction as? CommandInteraction)?.command ?: return@on

        if (command.rootName != "affix") return@on
        logger.info { "Running slash command \"affix\"" }

        interaction.respondPublic {
            content = command.makeResponse("cxs") { cxs -> "?affix $cxs" }
        }
    }

    kord.on<InteractionCreateEvent> {

        val command = (interaction as? CommandInteraction)?.command ?: return@on

        if (command.rootName != "ej") return@on
        logger.info { "Running slash command \"ej\"" }

        interaction.respondPublic {
            content = command.makeResponse("text") { text -> "?ej $text" }
        }
    }

    kord.on<InteractionCreateEvent> {

        val command = (interaction as? CommandInteraction)?.command ?: return@on

        if (command.rootName != "whosacutebot") return@on
        logger.info { "Running slash command \"whosacutebot\"" }

        interaction.respondPublic {
            content = respond("?!whosacutebot")!!
        }
    }

    kord.on<InteractionCreateEvent> {

        val command = (interaction as? CommandInteraction)?.command ?: return@on

        if (command.rootName != "date") return@on
        logger.info { "Running slash command \"date\"" }

        interaction.respondPublic {
            content = respond("?date")!!
        }
    }

}

private fun constructCommand(showDefaults: Boolean, precision: String, sgloss: Boolean = false): String {
    val prefix = if (showDefaults) "??" else "?"
    val command = when (precision) {
        "full" -> "full"
        "short" -> "short"
        else -> "gloss"
    }
    val s = if (sgloss) "s" else ""

    return "$prefix$s$command"
}

@KordPreview
private fun InteractionCommand.makeResponse(argName: String, stringCommand: (String) -> String): String {
    val arg = options[argName]?.string()

    return if (arg != null) {
        respond(stringCommand(arg)) ?: "*No response*"
    } else "*No argument found*"
}