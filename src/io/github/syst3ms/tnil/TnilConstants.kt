@file:Suppress("SpellCheckingInspection")

package io.github.syst3ms.tnil

import java.lang.AssertionError

const val ALT_VF_FORM = 1
const val SLOT_THREE_PRESENT = 2

const val SLOT_SEPARATOR = "-"
const val CATEGORY_SEPARATOR = "/"
const val REFERENT_SEPARATOR = "+"
const val REFERENT_START = "["
const val REFERENT_END = "]"
const val CA_STACKING_VOWEL = "üä"
const val LOW_TONE_MARKER = "_"
const val CARRIER_START = "\""
const val CARRIER_END = "\""
const val REGISTER_START = "{"
const val REGISTER_END = "}"
const val DISCURSIVE_START = "«"
const val DISCURSIVE_END = "»"
const val MODULAR_PLACEHOLDER = "@@"
const val REFERENT_ROOT_PLACEHOLDER = "@@"
const val TPP_SHORTCUT_PLACEHOLDER = "$$"
const val CONCATENATIVE_START = "{{"
const val CONCATENATIVE_END = "}}"
const val AFFIX_UNKNOWN_VOWEL_MARKER = "@"
const val AFFIX_UNKNOWN_CASE_MARKER = "&&"
const val AFFIX_UNKNOWN_CA_MARKER = "^"
const val AFFIX_STACKED_CA_MARKER = "##"
const val NOMINAL_FORMATIVE_IDENTIFIER = ":"
const val VERBAL_FORMATIVE_IDENTIFIER = "!"
const val TPP_AFFIX_CONSONANT = "kt"
const val RTI_AFFIX_CONSONANT = "lt"
const val VK_AFFIX_CONSONANT = "rl"
const val PRA_SHORTCUT_AFFIX_MARKER = "%%"
const val SPECIAL_AFFIX_SLOT_SEPARATOR = "—"

val VOWEL_FORM = listOf(
    "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
    "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "ia/oä", "iä/uä", "ie/oë", "ië/uë", "ëu", "uö/iö", "uo/io", "ue/eö", "ua/aö",
    "ao", "ae", "ea", "eo", "eë", "öe", "oe", "öa", "oa",
    "a'a", "ä'ä", "e'e", "ë'ë", "i'i", "ö'ö", "o'o", "ü'ü", "u'u",
    "a'i", "a'u", "e'i", "e'u", "ë'i", "o'u", "o'i", "i'u", "u'i",
    "i'a", "i'ä", "i'e", "i'ë", "ë'u", "u'ö", "u'o", "u'e", "u'a",
    "a'o", "a'e", "e'a", "e'o", "e'ë", "ö'e", "o'e", "ö'a", "o'a",
    "ayo", "aye", "eya", "eyo", "eyë", "öye", "oye", "öya", "oya"
)
val CONSONANTS = listOf(
    "p", "b", "t", "d", "k", "g", "'", "f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "h", "ļ",
    "c", "ẓ", "č", "j", "m", "n", "ň", "r", "l", "w", "y", "ř"
)
val FRICATIVES = listOf("f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "ř", "h", "ļ")
val AFFRICATES = listOf("c", "ẓ", "č", "j")
val NASALS = listOf("m", "n", "ň")
val STOPS = listOf("p", "b", "t", "d", "k", "g")
val CD_CONSONANTS = listOf(
        "h", "ç", "w", "y",
        "hw", "hh", "çw", "çç",
        "hl", "hr", "hm", "hn",
        "hlw", "hly", "hmw", "hmy"
)
val INVALID_LEXICAL_CONSONANTS = listOf("ļ", "ļw", "ļy", "ç", "çç", "çw", "w", "y")
val CASE_ACCESSOR_AFFIXES = listOf("ll", "rr", "tt", "dd")
val INVERSE_CASE_ACCESSOR_AFFIXES = listOf("kk", "gg", "pp", "bb")
val CASE_STACKING_AFFIXES = listOf("lw", "ly")
val AFFIX_VOWELS = listOf(
    "üa", "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
    "üe", "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "üo", "ia/oä", "iä/uä", "ie/oë", "ië/uë", "ëu", "uö/iö", "uo/io", "ue/eö", "ua/aö",
    "üö", "ao", "ae", "ea", "eo", "eë", "öe", "oe", "öa", "oa",
    "üyö", "ayo", "aye", "eya", "eyo", "eyë", "öye", "oye", "öya", "oya"
)
val SIMPLE_VV_FORMS = listOf(
        "a", "ä", "e", "i", "u", "ü", "o", "ö",
        "ai", "au", "ei", "eu", "ui", "iu", "oi", "ou",
        "ia", "iä", "ie", "ië", "ua", "ue", "uo", "uö",
        "oä", "uä", "oë", "uë", "aö", "eö", "iö", "io",
        "ao", "ae", "ea", "eo", "oa", "öa", "oe", "öe",
        "awa", "äwä", "ewe", "iwi", "uyu", "üwü", "owo", "öwö",
        "awi", "awu", "ewi", "ewu", "uwi", "iwu", "owi", "owu",
        "iwa", "iwä", "iwe", "iwë", "uya", "uye", "uyo", "uyö",
        "owä", "uwä", "owë", "uwë", "awö", "ewö", "iwo", "iwö",
        "awo", "awe", "ewa", "ewo", "owa", "öwa", "owe", "öwe"
)
val VR_FORMS = listOf(
        "a", "ä", "e", "i", "u", "ü", "o", "ö",
        "ai", "au", "ei", "eu", "ui", "iu", "oi", "ou",
        "ia/öa", "iä/uä", "ie/oë", "ië/uë", "ua/aö", "ue/eö", "uo/io", "uö/iö",
        "ao", "ae", "ea", "eo", "oa", "öa", "oe", "öe"
)
val COMBINATION_PRA_SPECIFICATION = listOf("bz", "gz", "bž", "gž")
val SCOPING_VALUES = listOf(
        "a", "u", "e", "i", "o", "ö",
        "h", "'h", "'w", "'y", "'hl", "'hr"
)

interface Precision {
    fun toString(precision: Int, ignoreDefault: Boolean = false): String
}

enum class Incorporation(private val short: String) : Precision {
    TYPE_ONE("T1"),
    TYPE_TWO("T2");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Version(private val short: String) : Precision {
    PROCESSUAL("PRC"),
    COMPLETIVE("CPT");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Relation(private val short: String) : Precision {
    UNFRAMED("UNF"),
    FRAMED("FRA");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Stem(private val short: String) : Precision {
    STEM_ONE("S1"),
    STEM_TWO("S2"),
    STEM_THREE("S3"),
    STEM_ZERO("S0");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Specification(private val short: String) : Precision {
    BASIC("BSC"),
    CONTENTIAL("CTE"),
    CONSTITUTIVE("CSV"),
    OBJECTIVE("OBJ");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}
enum class Function(private val short: String) : Precision {
    STATIVE("STA"),
    DYNAMIC("DYN");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Uniplex(private val short: String) : Precision {
    SPECIFIC("UXS"),
    POTENTIAL("UPX");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Similarity(private val short: String) : Precision {
    DUPLEX_SIMILAR("DPS"),
    DUPLEX_DISSIMILAR("DPD"),
    DUPLEX_FUZZY("DPF"),
    MULTIPLEX_SIMILAR("MPS"),
    MULTIPLEX_DISSIMILAR("MPD"),
    MULTIPLEX_FUZZY("MPF");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Connectedness(private val short: String) : Precision {
    SEPARATE("SEP"),
    CONNECTED("CND"),
    FUSED("FSD");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Affiliation(private val short: String) : Precision {
    CONSOLIDATIVE("CSL"),
    ASSOCIATIVE("ASO"),
    VARIATIVE("VAR"),
    COALESCENT("COA");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Extension(private val short: String) : Precision {
    DELIMITIVE("DEL"),
    PROXIMAL("PRX"),
    INCIPIENT("ICP"),
    ATTENUATIVE("ATV"),
    GRADUATIVE("GRA"),
    DEPLETIVE("DPL");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Perspective(private val short: String) : Precision {
    MONADIC("M"),
    POLYADIC("P"),
    NOMIC("N"),
    ABSTRACT("A");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Essence(private val short: String) : Precision {
    NORMAL("NRM"),
    REPRESENTATIVE("RPV");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Context(private val short: String) : Precision {
    EXISTENTIAL("EXS"),
    FUNCTIONAL("FNC"),
    REPRESENTATIONAL("RPS"),
    AMALGAMATIVE("AMG");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Valence(private val short: String) : Precision {
    MONOACTIVE("MNO"),
    PARALLEL("PRL"),
    COROLLARY("CRO"),
    RECIPROCAL("RCP"),
    COMPLEMENTARY("CPL"),
    DUPLICATIVE("DUP"),
    DEMONSTRATIVE("DEM"),
    CONTINGENT("CNG"),
    PARTICIPATIVE("PTI");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Phase(private val short: String) : Precision {
    CONTEXTUAL("CTX"),
    PUNCTUAL("PCT"),
    ITERATIVE("ITR"),
    REPETITIVE("REP"),
    INTERMITTENT("ITM"),
    RECURRENT("RCT"),
    FREQUENTATIVE("FRE"),
    FRAGMENTATIVE("FRG"),
    FLUCTUATIVE("FLC");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Effect(private val short: String) : Precision {
    NEUTRAL("NEU"),
    BENEFICIAL("BEN"),
    UNKNOWN("UNK"),
    DETRIMENTAL("DET");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Level(private val short: String) : Precision {
    EQUATIVE("EQU"),
    SURPASSIVE("SUR"),
    DEFICIENT("DFC"),
    MAXIMAL("MAX"),
    MINIMAL("MIN"),
    SUPERLATIVE("SPL"),
    INFERIOR("IFR"),
    SUPEREQUATIVE("SPQ"),
    SUBEQUATIVE("SUBEQUATIVE");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Aspect(private val short: String, val vt: String) : Precision {
    RETROSPECTIVE("RTR", "a"),
    PROSPECTIVE("PRS", "ä"),
    HABITUAL("HAB", "e"),
    PROGRESSIVE("PRG", "ë"),
    IMMINENT("IMM", "i"),
    PRECESSIVE("PCS", "ö"),
    REGULATIVE("REG", "o"),
    ANTECENDENT("ATC", "ü"),
    ANTICIPATORY("ATP", "u"),
    RESUMPTIVE("RSM", "ai"),
    CESSATIVE("CSS", "au"),
    PAUSAL("PAU", "ei"),
    REGRESSIVE("RGR", "eu"),
    PRECLUSIVE("PCL", "ëi"),
    CONTINUATIVE("CNT", "ou"),
    INCESSATIVE("ICS", "oi"),
    SUMMATIVE("SMM", "iu"),
    INTERRUPTIVE("IRP", "ui"),
    PREEMPTIVE("PMP", "ia/oä"),
    CLIMACTIC("CLM", "iä/uä"),
    DILATORY("DLT", "ie/oë"),
    TEMPORARY("TMP", "ië/uë"),
    MOTIVE("MTV", "ëu"),
    SEQUENTIAL("SQN", "uö/iö"),
    EXPEDITIVE("EPD", "uo/io"),
    PROTRACTIVE("PTC", "ue/eö"),
    PREPARATORY("PPR", "ua/aö"),
    DISCLUSIVE("DCL", "ao"),
    CONCLUSIVE("CCL", "ae"),
    CULMINATIVE("CUL", "ea"),
    INTERMEDIATIVE("IMD", "eo"),
    TARDATIVE("TRD", "eë"),
    TRANSITIONAL("TNS", "öe"),
    INTERCOMMUTATIVE("ITC", "oe"),
    EXPENDITIVE("XPD", "öa"),
    LIMITATIVE("LIM", "oa");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byVowel(vt: String) = values().find { it.vt eq vt }
    }
}

enum class Mood(private val short: String, val cn: String, val cy: String) : Precision {
    FACTUAL("FAC", "h/ç", ""),
    SUBJUNCTIVE("SUB", "hl", "x"),
    ASSUMPTIVE("ASM", "hr", "rs"),
    SPECULATIVE("SPC", "hw", "rš"),
    COUNTERFACTIVE("COU", "hm", "rz"),
    HYPOTHETICAL("HYP", "hn", "rž");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byCn(cn: String) = values().find { it.cn eq cn }

        fun byCy(cy: String) = values().find { it.cy eq cy }
    }
}

enum class CaseScope(private val short: String, val cn: String, val cy: String) : Precision {
    CCH("CCh", "h/ç", ""),
    CCL("CCl", "hl", "x"),
    CCR("CCr", "hr", "rs"),
    CCW("CCw", "hw", "rš"),
    CCM("CCm", "hm", "rz"),
    CCn("CCn", "hn", "rž");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byCn(cn: String) = values().find { it.cn eq cn }

        fun byCy(cy: String) = values().find { it.cy eq cy }
    }
}

enum class Case(private val short: String, val vc: String, val vfShort: String? = null) : Precision {
    THEMATIC("THM", "a"),
    INSTRUMENTAL("INS", "ä"),
    ABSOLUTIVE("ABS", "e"),
    STIMULATIVE("STM", "ë"),
    AFFECTIVE("AFF", "i"),
    EFFECTUATIVE("EFF", "ö"),
    ERGATIVE("ERG", "o"),
    DATIVE("DAT", "ü"),
    INDUCIVE("IND", "u"),
    POSSESSIVE("POS", "ai"),
    PROPRIETIVE("PRP", "au"),
    GENITIVE("GEN", "ei"),
    ATTRIBUTIVE("ATT", "eu"),
    PRODUCTIVE("PDC", "ëi"),
    INTERPRETIVE("ITP", "ou"),
    ORIGINATIVE("OGN", "oi"),
    INTERDEPENDENT("IDP", "iu"),
    PARTITIVE("PAR", "ui"),
    APPLICATIVE("APL", "ia/oä"),
    PURPOSIVE("PUR", "iä/uä"),
    TRANSMISSIVE("TRA", "ie/oë"),
    DEFERENTIAL("DFR", "ië/uë"),
    CONTRASTIVE("CRS", "ëu"),
    TRANSPOSITIVE("TSP", "uö/iö"),
    COMMUTATIVE("CMM", "uo/io"),
    COMPARATIVE("CMP", "ue/eö"),
    CONSIDERATIVE("CSD", "ua/aö"),
    FUNCTIVE("FUN", "ao", "ai"),
    TRANSFORMATIVE("TFM", "ae", "au"),
    CLASSIFICATIVE("CLA", "ea", "ei"),
    RESULTATIVE("RSL", "eo", "eu"),
    CONSUMPTIVE("CSM", "eë", "ëi"),
    CONCESSIVE("CON", "öe"),
    AVERSIVE("AVS", "oe"),
    CONVERSIVE("CVS", "öa"),
    SITUATIVE("SIT", "oa"),
    LOCATIVE("LOC", "a'a", "i"),
    ATTENDANT("ATD", "ä'ä"),
    ALLATIVE("ALL", "e'e"),
    ABLATIVE("ABL", "ë'ë"),
    ORIENTATIVE("ORI", "i'i"),
    INTERRELATIVE("IRL", "ö'ö"),
    INTRATIVE("INV", "o'o"),
    NAVIGATIVE("NAV", "u'u"),
    CONCURSIVE("CNR", "a'i", "iu"),
    ASSESSIVE("ASS", "a'u"),
    PERIODIC("PER", "e'i"),
    PROLAPSIVE("PRO", "e'u"),
    PRECURSIVE("PCV", "ë'i"),
    POSTCURSIVE("PCR", "o'u"),
    ELAPSIVE("ELP", "o'i"),
    PROLIMITIVE("PLM", "u'i"),
    REFERENTIAL("REF", "i'a", "a"),
    ASSIMILATIVE("ASI", "i'ä", "ä"),
    ESSIVE("ESS", "i'e", "e"),
    CORRELATIVE("COR", "i'ë", "ë"),
    COMPOSITIVE("CPS", "ë'u", "ëu"),
    COMITATIVE("COM", "u'ö", "ö"),
    UTILITATIVE("UTL", "u'o", "o"),
    RELATIVE("RLT", "u'a", "u"),
    ACTIVATIVE("ACT", "a'o", "ui"),
    DESCRIPTIVE("DSC", "a'e", "oi"),
    TERMINATIVE("TRM", "e'a", "ou"),
    SELECTIVE("SEL", "e'o"),
    CONFORMATIVE("CFM", "e'ë"),
    DEPENDENT("DEP", "ö'e"),
    PREDICATIVE("PRD", "o'e"),
    VOCATIVE("VOC", "o'a");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byVowel(vc: String, vfShort: Boolean = false) = values().find { (if (vfShort) it.vfShort ?: "" else it.vc) eq vc }
    }
}

enum class Illocution(private val short: String) : Precision {
    ASSERTIVE("ASR"),
    PERFORMATIVE("PFM");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Expectation(private val short: String) : Precision {
    COGNITIVE("COG"),
    RESPONSIVE("RSP"),
    EXECUTIVE("EXE");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Validation(private val short: String) : Precision {
    OBSERVATIONAL("OBS"),
    RECOLLECTIVE("REC"),
    PURPORTIVE("PUP"),
    REPORTIVE("RPR"),
    CONVENTIONAL("CVN"),
    INFERENTIAL("INF"),
    INTUITIVE("ITU"),
    IMAGINARY("IMA");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Bias(private val short: String, val cb: String) : Precision {
    DOLOROUS("DOL", "řřx"),
    SKEPTICAL("SKP", "rnž"),
    IMPATIENT("IPT", "žžv"),
    REVELATIVE("RVL", "mmļ"),
    TREPIDATIVE("TRP", "llč"),
    REPULSIVE("RPU", "šštļ"),
    DESPERATIVE("DES", "mřř"),
    DISAPPROBATIVE("DPB", "ffx"),
    PROSAIC("PSC", "žžt"),
    COMEDIC("CMD", "pļļ"),
    PROPOSITIVE("PPV", "sl"),
    SUGGESTIVE("SGS", "ltç"),
    DIFFIDENT("DFD", "cč"),
    REFLECTIVE("RFL", "llm"),
    EUPHEMISTIC("EUP", "vvt"),
    CORRECTIVE("CRR", "ňţ"),
    CONTEMPTIVE("CTP", "kšš"),
    EXASPERATIVE("EXA", "kçç"),
    INDIGNATIVE("IDG", "pšš"),
    DISMISSIVE("DIS", "kff"),
    DERISIVE("DRS", "pfc"),
    PESSIMISTIC("PES", "ksp"),
    DUBITATIVE("DUB", "mmf"),
    INVIDIOUS("IVD", "řřn"),
    DISCONCERTIVE("DCC", "gzj"),
    STUPEFACTIVE("STU", "ļļč"),
    FASCINATIVE("FSC", "žžj"),
    INFATUATIVE("IFT", "vvr"),
    EUPHORIC("EUH", "gzz"),
    DELECTATIVE("DLC", "ẓmm"),
    ATTENTIVE("ATE", "ňj"),
    APPROBATIVE("APB", "řs"),
    IRONIC("IRO", "mmž"),
    PRESUMPTIVE("PSM", "nnţ"),
    GRATIFICATIVE("GRT", "mmh"),
    SATIATIVE("SAT", "ff"),
    PERPLEXIVE("PPX", "llh"),
    CONTEMPLATIVE("CTV", "gvv"),
    PROPITIOUS("PPT", "mll"),
    SOLLICITATIVE("SOL", "ňňs"),
    REACTIVE("RAC", "kll"),
    COINCIDENTAL("COI", "ššč"),
    FORTUITOUS("FOR", "lzp"),
    ANNUNCIATIVE("ANN", "drr"),
    OPTIMAL("OPT", "ççk"),
    CONTENSIVE("CNV", "rrj"),
    RENUNCIATIVE("RNC", "mzt"),
    MANDATORY("MND", "msk"),
    EXIGENT("EXG", "rrs"),
    INSIPID("ISP", "lçp"),
    ADMISSIVE("ADM", "lļ"),
    APPREHENSIVE("APH", "vvz"),
    IMPLICATIVE("IPL", "vll"),
    ACCIDENTAL("ACC", "lf"),
    ANTICIPATIVE("ANP", "lst"),
    ARCHETYPAL("ACH", "mçt"),
    VEXATIVE("VEX", "ksk"),
    CORRUPTIVE("CRP", "gžž"),
    DEJECTIVE("DEJ", "žžg");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byGroup(cb: String) = values().find { it.cb eq cb }
    }
}

enum class Register(private val short: String, val initial: String, val final: String) : Precision {
    DISCURSIVE("DSV", "a", "ai"),
    PARENTHETICAL("PNT", "e", "ei"),
    COGITANT("CGT", "o", "oi"),
    EXAMPLIFICATIVE("EXM", "ö", "ëi"),
    SPECIFICATIVE("SPF", "i", "iu"),
    MATHEMATICAL("MTH", "u", "ui"),
    CARRIER_END("CAR", "", "ü");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byVowel(v: String): Pair<Register, Boolean>? {
            val i = values().indexOfFirst { it.initial eq v }
            return if (i != -1) {
                values()[i] to true
            } else {
                values().find { it.final eq v }?.to(false)
            }
        }
    }
}

enum class Referent(private val short: String) : Precision {
    MONADIC_SPEAKER("1m"),
    MONADIC_ADDRESSEE("2m"),
    POLYADIC_ADDRESSEE("2p"),
    MONADIC_ANIMATE_THIRD_PARTY("ma"),
    POLYADIC_ANIMATE_THIRD_PARTY("pa"),
    MONADIC_INANIMATE_THIRD_PARTY("mi"),
    POLYADIC_INANIMATE_THIRD_PARTY("pi"),
    MIXED_THIRD_PARTY("Mx"),
    OBVIATIVE("Obv"),
    ANIMATE_IMPERSONAL("IPa"),
    INANIMATE_IMPERSONAL("IPi"),
    NOMIC_REFERENT("Nai"),
    ABSTRACT_REFERENT("Aai");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

fun parsePraShortcut(c: String, v: String, precision: Int): String? {
    val i = AFFIX_VOWELS.indexOfFirst { it eq v }
    assert(i / 10 == 2 && i != 20)
    val case = when (i % 10) {
        1 -> Case.POSSESSIVE
        2 -> Case.PROPRIETIVE
        3 -> Case.GENITIVE
        4 -> Case.ATTRIBUTIVE
        5 -> Case.PRODUCTIVE
        6 -> Case.INTERPRETIVE
        7 -> Case.ORIGINATIVE
        8 -> Case.COMITATIVE
        9 -> Case.CORRELATIVE
        else -> throw AssertionError()
    }
    val ref = parsePersonalReference(c) ?: return null
    return "(" + join(ref.toString(precision), case.toString(precision)) + ")"
}