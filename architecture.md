Hi. If you are reading this, you probably want to develop the bot, or just figure out how it works. The code should be readable if you want to dive right in, but I hope an overview will help.

The bot has a layered structure, with IO on the top and the raw language categories on the bottom. The code is mostly functional in style, apart from a few mutating indices and such here and there. The layers are as follows:

### Layer 6: Interfaces

There are currently three implemented interfaces to the bot: the command line interface, responding to Discord messages, and Discord slash commands. Each produces command strings for the Dispatch layer, and displays whatever it gets back.

### Layer 5: Dispatch

A fully text-based interface that contains the actual commands. Basically just a big "when" block. If the command is to gloss, it sends the strings of words downwards, gets back the Gloss objects and asks them nicely to become gloss strings. All logic relating to the dictionaries is on this layer too.

### Layer 4: Formatting

This layer clears the raw words of any punctuation, alternate versions of letters and stress markings, producing nice, clean lists of consonant clusters and vowelforms. The type of a given word is also figured out on this level.

### Layer 3: Context

In a few cases, words affect the interpretation of nearby words, i.e. modular adjuncts, suppletive adjuncts and the carrier root. This layer handles that before calling the word-by-word glossing level.

### Layer 2: Words

Each word is parsed slot by slot by calling the matching function for its word type. The main difficulty on this level is figuring out the various messy rules as to what slot goes where. Each slot or affix is then parsed in its own function

### Layer 1: Slots (and Affixes)

This layer works out the mapping of phonemic forms to the underlying grammatical categories.

### Layer 0: Categories

Here are finally the actual categories, and the actual logic for glossing them! The Dispatch level calls a Gloss to be glossed, which calls its Slots, which ultimately call the toString methods of the categories, passing the settings (precision and whether defaults are shown) down each layer.

That might be a lot to keep in one's head, but you generally don't have to worry about the other layers when working on one. I wish you luck.

-- Behemoth