### To-Do list

**v0.18.1**

- Series 5-8 removed
- Vc glottal stop can be moved to anywhere in the word after slot II
- ~~Case affix values changed~~

**Language to-dos**

- Implement absolute level
- Add in Vk glottalization
- Implement suppletive adjunct forms into PRAs (potentially eliminate them as a distinct class?)
 
**Code to-dos**

 - ~~Create "Slot" class and make "stem used" marking sensible~~
 - Re-write sentence parsing
    - Move stress parsing from individual word types to general word level
    - Reimplement Mood/Case-Scope distinction in modular adjuncts
 - ~~Rewrite parseAffixual and parseAffixualScoping~~
 - ~~Help testing with "glossesTo" infix function~~
 - ~~Separate determining the type of a word into a separate function (for testability)~~
 - ~~Create an actual error type instead of mucking about with null characters~~
 - ~~Move actual glossing (toString) two or so levels up in the hierarchy~~
 - ~~Rootmode should really not use strings...~~

**Ideas**

- External Juncture checker
- Pronunciation guide
- Explicit case-accessor definitions
- Dynamic grammatical affixes?