# IthkuilGloss

A Discord parser bot for the Ithkuil IV language.

Currently up to date to morphophonology v0.18.5.

See [the help file](https://github.com/ngoriyasjil/IthkuilGloss/blob/master/resources/help.md) for the list of commands!

## How to run

1. If you’re running the Discord bot (rather than the CLI), place the bot token in the file `resources/token.txt`.
2. Compile with Maven.

## How to run the Command Line Interface

1. Set `main.class` to `ithkuil.iv.gloss.interfaces.TerminalKt` in `pom.xml`
2. Compile with Maven and run

The CLI will by default download the lexicon locally, and then use that. To update the local lexicon, use the `?!reload` command.

Ithkuil IV language is created by John Quijada.

Based on TNILGloss by Syst3ms