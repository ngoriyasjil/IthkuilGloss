@file:Suppress("SpellCheckingInspection")

package ithkuil.iv.gloss

const val SLOT_SEPARATOR = "-"
const val CATEGORY_SEPARATOR = "."
const val AFFIX_DEGREE_SEPARATOR = "/"
const val STRESS_SLOT_SEPARATOR = "\\"
const val REFERENT_SEPARATOR = "+"
const val REFERENT_START = "["
const val REFERENT_END = "]"
const val CONCATENATION_SEPARATOR = "—"
const val LOW_TONE_MARKER = "_"
const val CA_STACKING_VOWEL = "üä"
const val CARRIER_ROOT_CR = "s"

val ITHKUIL_CHARS = setOf(
    "p", "b", "t", "d", "k", "g", "f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "h", "ļ",
    "c", "ẓ", "č", "j", "m", "n", "ň", "r", "l", "w", "y", "ř",
    "a", "ä", "e", "ë", "i", "ï", "u", "ü", "o", "ö",
    "á", "â", "é", "ê", "í", "î", "ú", "û", "ó", "ô",
    "'", "-"
)

//Substitutions:

val ALLOGRAPHS = listOf(
    "\u200B" to "",
    "’" to "'", // U+2019 RIGHT SINGLE QUOTATION MARK (used by JQ and others and cross-linguistically)
    "ʼ" to "'", // U+02BC MODIFIER LETTER APOSTROPHE (used by uagle and cross-linguistically)
    "á" to "á",
    "ä" to "ä", "â" to "â",
    "é" to "é",
    "ë" to "ë", "ê" to "ê",
    "ï" to "ï", "î" to "î",
    "[ìı]|ì" to "i", "í" to "í",
    "ó" to "ó", "ö" to "ö", "ô" to "ô",
    "ù|ù" to "u", "ú" to "ú", "ü" to "ü", "û" to "û",
    "č" to "č",
    "ç" to "ç", "[ṭŧț]|ţ|ṭ" to "ţ",
    "[ḍđ]|ḍ|ḑ" to "ḑ",
    "[łḷ]|ḷ|ļ" to "ļ",
    "š" to "š",
    "ž" to "ž",
    "ż|ẓ" to "ẓ",
    "ṇ|ň|ņ|ṇ" to "ň",
    "ṛ|ř|ŗ|r͕|ŗ|ṛ" to "ř",
)

val CA_SUBSTITUTIONS = listOf(
    "řd" to "řtt", "řg" to "řkk", "řb" to "řpp",
    "rd" to "rtt", "rg" to "rkk", "rb" to "rpp", "ňv" to "rňm", "ňḑ" to "rňn",
    "ld" to "ltt", "lg" to "lkk", "lb" to "lpp", "nḑ" to "rnm", "mḑ" to "rmn",
    "^nd" to "tt", "^ng" to "kk", "^mb" to "pp", "nz" to "nn", "mz" to "mm",
    "ẓ" to "ňy", "ž" to "çy", "j" to "tçy", "gž" to "kçy", "bž" to "pçy",
    "mv" to "np", "ňz" to "ňk", "v(?=.)" to "nf", "fs" to "tf", "fš" to "kf",
    "c" to "ts", "č" to "tš", "ḑ" to "tţ"
)


val UNSTRESSED_FORMS = listOf(
    "á" to "a", "â" to "ä",
    "é" to "e", "ê" to "ë",
    "í" to "i", "î" to "ï",
    "ô" to "ö", "ó" to "o",
    "û" to "ü", "ú" to "u"
)

//Vowels

val VOWEL_FORMS = listOf(
    "a", "ä", "e", "ï", "i", "ö", "o", "ü", "u",
    "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "ia/oä", "iä/uä", "ie/oë", "ië/uë", "ëu", "uö/iö", "uo/io", "ue/eö", "ua/aö",
    "ao", "ae", "ea", "eo", "eë", "öe", "oe", "öa", "oa"
)

val VOWELS = setOf(
    "a", "ä", "e", "ë", "i", "ï", "ö", "o", "ü", "u",
    "á", "â", "é", "ê", "í", "î", "ú", "û", "ó", "ô"
)

val VOWELS_AND_GLOTTAL_STOP = VOWELS + "'"

val DIPHTHONGS = setOf("ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui", "ëu")

val DEGREE_ZERO_CS_ROOT_FORMS = setOf("üa", "üe", "üo", "üö")

val SPECIAL_VV_VOWELS = setOf("ëi", "eë", "ëu", "öë", "eä", "öä")

//Consonants

val CONSONANTS = listOf(
    "p", "b", "t", "d", "k", "g", "'", "f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "h", "ļ",
    "c", "ẓ", "č", "j", "m", "n", "ň", "r", "l", "w", "y", "ř", "'"
)

val CC_CONSONANTS = setOf("w", "y", "h", "hl", "hm", "hw", "hr", "hn")

val CP_CONSONANTS = setOf("hl", "hm", "hn", "hň")

val COMBINATION_REFERENTIAL_SPECIFICATION = listOf("x", "xx", "lx", "rx")

val CASE_AFFIXES = setOf(
    "sw", "zw", "šw", "žw", "lw",
    "sy", "zy", "šy", "žy", "ly"
)

val CN_CONSONANTS = setOf(
    "h", "hl", "hr", "hm", "hn", "hň",
    "w", "y", "hw", "hlw", "hly", "hnw", "hny"
)

val CN_PATTERN_ONE = setOf(
    "h", "hl", "hr", "hm", "hn", "hň"
)

val CZ_CONSONANTS = setOf("h", "'h", "'w", "'y", "hw", "'hw")

//Other

val SENTENCE_START_GLOSS = GlossString("[sentence start]", "[sentence:]", "[S]")