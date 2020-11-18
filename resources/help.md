## **Help**

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

Precision:
  - *Default precision*: all morphological components except affixes are abbreviated, roots may change depending on the stem
  - *Strong precision*: all morphological components are completely written out, roots may change depending on the stem
  - *Weak precision*: all morphological components are abbreviated, roots will only display their generic title

SPLITMESSAGEHERE

Formatting details:
  - Bold text in place of a root/affix means that it was not found in the current database
  - Underlined text means that the corresponding category was taken into account when looking for a description of the root.
   For example, " 'description'/S2 " indicates that S2 contributed nothing to the final result of 'description' ;
   However, " 'description'/<ins>S2</ins> " indicates that 'description' was specifically picked because S2 was specified.
   NOTE: this only applies to Stem, and in the special case of -N- and -D-, Perspective

The parsing logic is far from perfect (and also difficult to improve substantially), so if an error message looks like nonsense to you,
it's probably actual nonsense caused by the algorithm not interpreting the structure of your input properly. If however the error pertains to
the actual type of word you are trying to parse, there may be an actual bug, to which case make sure to let me (Syst3ms#9959) know.

Currently maintained by Behemoth4#6479.