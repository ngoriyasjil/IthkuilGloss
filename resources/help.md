**IthkuilGloss Help**

The IthkuilGloss bot gives morpheme-by-morpheme glosses of Ithkuil IV text. A gloss exposes the underlying grammatical values, which helps both in understanding and composing Ithkuil text.

Main commands:
- Glosses the following words one at a time:
   - **?gloss**: regular precision
   - **?full**: full precision
   - **?short**: short precision
   
- Glosses the following sentences linearly:
   - **?s** or **?sgloss**: regular precision
   - **?sfull**: full precision
   - **?sshort**: short precision
  
> Command: ?gloss lalo
> Reply: **lalo:** __S1__-“adult human“-ERG

By default, the commands don't show default values. To show all values, use the prefix "??" (e.g. "??gloss") instead of "?".

For glossing short examples amidst text, any text in between ":?" and "?:" is glossed linearly with regular precision (equivalent to **?s**). Multiple such glosses can be used in one message.

Precision:
  - __Regular precision__: all morphological components except affixes are abbreviated
  - __Full precision__: all morphological components are completely written out
  - __Short precision__: all morphological components including affixes are abbreviated, roots will only display their generic title

Other commands:
  - **?root**, **?affix**: look up the definition of a root or affix by its consonantal value
  - **?!reload**: updates the root and affix dictionaries from the spreadsheet
  - **?date**: gives the current UTC time and date in Ithkuil IV
  - **?ej**: checks a text for violations of External Juncture (Sec. 1.5)
  - **?whosacutebot**: tells the bot that it is such a cute bot
    
You can delete a message by the bot that is a reply to you by reacting to it with an ``:x:`` emoji.

Formatting details:
  - Bold text in place of a root/affix means that it was not found in the current database
  - Underlined text means that the category was taken into account when looking for a description of the root.
   For example, "S2-'description' " indicates that S2 contributed nothing to the final result of 'description'; However, "__S2__-'description'" indicates that 'description' was specifically picked because S2 was specified.

For glossing Ithkuil III, there is the command "!gloss". This is a separate system which is not maintained by us (or anyone).

The bot is an amateur coding project, and may suffer from severe bugs at any time. If you spot one, please contact me (@IshtarAletheia#0347), so I can hopefully go about fixing it.

GitHub repo at https://github.com/ngoriyasjil/IthkuilGloss

Crowd-sourced dictionary used by the bot at https://docs.google.com/spreadsheets/d/1JdaG1PaSQJRE2LpILvdzthbzz1k_a0VT86XSXouwGy8/edit?usp=sharing
