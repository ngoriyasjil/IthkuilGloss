package ithkuil.iv.gloss.interfaces

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.respond
import dev.kord.core.entity.interaction.boolean
import dev.kord.core.entity.interaction.string
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import ithkuil.iv.gloss.dispatch.logger
import ithkuil.iv.gloss.dispatch.respond
import kotlinx.coroutines.flow.collect


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
            string("text", "The Ithkuil text to gloss") {
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

        command("whosagoodbot", "Tells the bot how good a bot it is. :3") { }

        command("date", "Tells the current time and date (UTC) in Ithkuil") { }

    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "gloss") return@on

        val showDefaults = interaction.command.options["show-defaults"]?.boolean() ?: false
        val precision = interaction.command.options["precision"]?.string() ?: "regular"

        val command = constructCommand(showDefaults, precision)

        interaction.respond {
            content = commandResponse("words") { words -> "$command $words" }
        }
    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "sgloss") return@on

        val showDefaults = interaction.command.options["show-defaults"]?.boolean() ?: false
        val precision = interaction.command.options["precision"]?.string() ?: "regular"

        val command = constructCommand(showDefaults, precision, sgloss = true)

        interaction.respond {
            content = commandResponse("words") { words -> "$command $words" }
        }
    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "root") return@on
        logger.info { "Running slash command \"root\"" }

        interaction.respond {
            content = commandResponse("crs") { crs -> "?root $crs" }
        }
    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "affix") return@on
        logger.info { "Running slash command \"affix\"" }

        interaction.respond {
            content = commandResponse("cxs") { cxs -> "?affix $cxs" }
        }
    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "ej") return@on
        logger.info { "Running slash command \"ej\"" }

        interaction.respond {
            content = commandResponse("text") { text -> "?ej $text" }
        }
    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "whosagoodbot") return@on
        logger.info { "Running slash command \"whosagoodbot\"" }

        interaction.respond {
            content = respond("?!whosagoodbot")!!
        }
    }

    kord.on<InteractionCreateEvent> {
        if (interaction.command.rootName != "date") return@on
        logger.info { "Running slash command \"date\"" }

        interaction.respond {
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
private fun InteractionCreateEvent.commandResponse(argName: String, stringCommand: (String) -> String): String {
    val arg = interaction.command.options[argName]?.string()

    return if (arg != null) {
        respond(stringCommand(arg)) ?: "*No response*"
    } else "*No argument found*"
}