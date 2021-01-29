**Help**

Prefixes: 
  - **?**: commands used with this prefix won't write the default values of some grammatical categories.
  - **??**: commands used with this prefix will write the values of all morphological categories even when default.

General commands (must be preceded by the proper prefix):
  - **gloss**: gives a morphological analysis of each subsequent word, with default precision
  - **full**: gives a morphological analysis of each subsequent word, with strong precision
  - **short**: gives a morphological analysis of each subsequent word, with weak precision
  - **s** or **sgloss**: gives a morphological analysis of the whole following sentence, with default precision
  - **sfull**: gives a morphological analysis of the whole following sentence, with strong precision
  - **sshort**: gives a morphological analysis of the whole following sentence, with weak precision
  - **root**, **affix**: look up the definition of a root or affix in the respective document as used by the bot

(Sentence parsing is currently only partially functional)

Precision:
  - *Default precision*: all morphological components except affixes are abbreviated, roots may change depending on the stem
  - *Strong precision*: all morphological components are completely written out, roots may change depending on the stem
  - *Weak precision*: all morphological components are abbreviated, roots will only display their generic title

Other commands:
  - **?!reload**: updates the root and affix documents from the spreadsheet
  - **?date**: gives the current UTC time and date in Ithkuil IV
  - **?!whosacutebot**: tells the bot that it is such a cute bot
    
You can delete a message that is a reply to you by reacting to it with an ``:x:`` emoji.

Formatting details:
  - Bold text in place of a root/affix means that it was not found in the current database
  - Underlined text means that the corresponding category was taken into account when looking for a description of the root.
   For example, " 'description'/S2 " indicates that S2 contributed nothing to the final result of 'description' ; However, " 'description'/__S2__ " indicates that 'description' was specifically picked because S2 was specified.
   NOTE: this currently only applies to Stem.

The bot is currently in development, and may suffer from severe bugs. If you spot one, please contact me (Behemoth#6479), so I can hopefully go about fixing it.
