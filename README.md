# TNILGloss

A Discord parser bot for the new Ithkuil language

Currently up to date to morphophonology v0.18.3.

## How to run

1. If you’re running the Discord bot (rather than the CLI), place the token
   file in `resources/token.txt`.
2. Compile with Maven.

## How to run Command Line Interface

1. Set `main.class` to `io.github.syst3ms.tnil.CommandLineInterfaceKt`
2. Compile with Maven and run

The CLI will by default download the lexicon locally, and then use that. To update the local lexicon, use the `?!reload` command.
