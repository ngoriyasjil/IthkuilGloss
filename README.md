# TNILGloss

A Discord parser bot for the new Ithkuil language

Currently being updated to morphophonology v0.16.0. **DO NOT USE**

## How to run

1. If you’re running the Discord bot (rather than the CLI), place the token
   file in `resources/token.txt`. If you’re not using Docker, also optionally
   provide `resources/roots.tsv` and `resources/affixes.tsv`.
2. Compile with Docker or Maven.

## How to run Command Line Interface

1. Provide `resources/roots.tsv` and `resources/affixes.tsv`
2. Set `<main.class>` to `io.github.syst3ms.tnil.CommandLineInterfaceKt`
3. Compile with Maven and run
