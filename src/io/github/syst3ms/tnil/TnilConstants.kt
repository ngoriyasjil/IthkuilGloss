@file:Suppress("SpellCheckingInspection", "unused")

package io.github.syst3ms.tnil

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
const val CONCATENATIVE_END = "}}"
const val AFFIX_UNKNOWN_VOWEL_MARKER = "@"
const val AFFIX_UNKNOWN_CASE_MARKER = "&&"
const val AFFIX_UNKNOWN_CA_MARKER = "^"
const val RTI_AFFIX_CONSONANT = "lt"

val VOWEL_FORMS = listOf(
        "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
        "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
        "ia/oä", "iä/uä", "ie/oë", "ië/uë", "ëu", "uö/iö", "uo/io", "ue/eö", "ua/aö",
        "ao", "ae", "ea", "eo", "eë", "öe", "oe", "öa", "oa",
        "a'a", "ä'ä", "e'e", "ë'ë", "i'i", "ö'ö", "o'o", "ü'ü", "u'u",
        "a'i", "a'u", "e'i", "e'u", "ë'i", "o'u", "o'i", "i'u", "u'i",
        "i'a", "i'ä", "i'e", "i'ë", "ë'u", "u'ö", "u'o", "u'e", "u'a",
        "a'o", "a'e", "e'a", "e'o", "e'ë", "ö'e", "o'e", "ö'a", "o'a",
)

val SPECIAL_VV_VOWELS = setOf("ëi", "eë", "ëu", "öë", "eä", "öä")

val CONSONANTS = listOf(
    "p", "b", "t", "d", "k", "g", "'", "f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "h", "ļ",
    "c", "ẓ", "č", "j", "m", "n", "ň", "r", "l", "w", "y", "ř", "'"
)
val FRICATIVES = setOf('f', 'v', 'ţ', 'ḑ', 's', 'z', 'š', 'ž', 'ç', 'x', 'ř', 'h', 'ļ')
val AFFRICATES = setOf('c', 'ẓ', 'č', 'j')
val NASALS = setOf('m', 'n', 'ň')
val STOPS = setOf('p', 'b', 't', 'd', 'k', 'g')
val CC_CONSONANTS = setOf("w", "y", "h", "hl", "hm", "hw", "hr", "hn")

val INVALID_LEXICAL_CONSONANTS = listOf("ļ", "ļw", "ļy", "ç", "çç", "çw", "w", "y")

val COMBINATION_PRA_SPECIFICATION = listOf("x", "xx", "lx", "rx")

val CASE_AFFIXES = setOf("ll", "lw", "sw", "zw", "šw", "rr", "ly", "sy", "zy", "šy")

val UNGLOTTAL_MAP = mapOf(
        "rrç" to "pk" , "llç" to "tk" , "řřţ" to "kt",
        "rrt" to "pt" , "řřf" to "kp" , "llf" to "tp",
        "llz" to "lpk", "lls" to "ltk", "llḑ" to "lkt",
        "rrz" to "rpk", "rrs" to "rtk", "rrḑ" to "rkt",
        "řřz" to "řpk", "řřs" to "řtk", "řřḑ" to "řkt",
        "llž" to "lpt", "llš" to "lkp", "llv" to "ltp",
        "rrž" to "rpt", "rrš" to "rkp", "rrv" to "rtp",
        "řřž" to "řpt", "řřš" to "řkp", "řřv" to "řtp",
        "vvm" to "bm" , "ḑḑm" to "dm" , "žžm" to "gm",
        "vvn" to "bn" , "ḑḑn" to "dn" , "žžn" to "gn")

val CA_SUBSTITUTIONS = listOf(
        "řd" to "řtt", "řg" to "řkk", "řb" to "řpp",
        "rd" to "rtt", "rg" to "rkk", "rb" to "rpp", "ňv" to "rňm", "nḑ" to "rňn",
        "ld" to "ltt", "lg" to "lkk", "lb" to "lpp", "ňž" to "rnm", "mž" to "rmn",
        "^nd" to "tt", "^ng" to "kk", "^mb" to "pp", "nz" to "nn", "mz" to "mm",
        "ňž" to "ňy", "ž" to "çy", "ẓ" to "cy", "j" to "čy",
        "mv" to "np", "ňz" to "ňk", "v(?=.)" to "nf", "fs" to "tf", "fš" to "kf",
        "c" to "ts", "tš" to "č", "ḑ" to "tţ")


val CN_CONSONANTS = setOf(
        "h", "hl", "hr", "hm", "hn", "hň",
        "w", "y", "hw", "hlw", "hly", "hnw", "hny"
)

val CZ_CONSONANTS = setOf("h", "'h", "'w", "'y", "'hl", "'hl")

val ITHKUIL_CHARS = setOf(
        "p", "b", "t", "d", "k", "g", "f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "h", "ļ",
        "c", "ẓ", "č", "j", "m", "n", "ň", "r", "l", "w", "y", "ř",
        "a", "ä", "e", "ë", "i", "u", "ü", "o", "ö",
        "'", "-"
)

class Affix(private val vx: String,
            private val cs : String,
            var canBePraShortcut: Boolean = false,
            private val noType: Boolean = false) : Precision { //Definitely not final

    override fun toString(precision: Int, ignoreDefault: Boolean): String
            = parseAffix(cs.defaultForm(), vx.defaultForm(), precision, ignoreDefault, canBePraShortcut = canBePraShortcut, noType = noType)
}

enum class Shortcut {
    Y_SHORTCUT,
    W_SHORTCUT;
}

class PrecisionString(
    private val full: String,
    private val short: String = full,
    private val ignorable: Boolean = false
) : Precision {

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return when {
            ignorable && ignoreDefault -> ""
            precision < 2 -> short
            else -> full
        }
    }
}

enum class Concatenation(override val short: String) : Category {
    TYPE_ONE("T1"),
    TYPE_TWO("T2");
}

enum class Version(override val short: String) : Category {
    PROCESSUAL("PRC"),
    COMPLETIVE("CPT");
}

enum class Relation(override val short: String) : Category {
    UNFRAMED("UNF"),
    FRAMED("FRA");
}

enum class Stem(override val short: String) : NoDefault {
    STEM_ZERO("S0"),
    STEM_ONE("S1"),
    STEM_TWO("S2"),
    STEM_THREE("S3");
}

enum class Specification(override val short: String) : Category {
    BASIC("BSC"),
    CONTENTIAL("CTE"),
    CONSTITUTIVE("CSV"),
    OBJECTIVE("OBJ");
}
enum class Function(override val short: String) : Category {
    STATIVE("STA"),
    DYNAMIC("DYN");
}

enum class Configuration(override val short: String) : Category {
    UNIPLEX("UNI"),
    DUPLEX_SIMILAR_SEPARATE("DSS"),
    DUPLEX_SIMILAR_CONNECTED("DSC"),
    DUPLEX_SIMILAR_FUSED("DSF"),
    DUPLEX_DISSIMILAR_SEPARATE("DDS"),
    DUPLEX_DISSIMILAR_CONNECTED("DDC"),
    DUPLEX_DISSIMILAR_FUSED("DDF"),
    DUPLEX_FUZZY_SEPARATE("DFS"),
    DUPLEX_FUZZY_CONNECTED("DFC"),
    DUPLEX_FUZZY_FUSED("DFF"),
    MULTIPLEX_SIMILAR_SEPARATE("MSS"),
    MULTIPLEX_SIMILAR_CONNECTED("MSC"),
    MULTIPLEX_SIMILAR_FUSED("MSF"),
    MULTIPLEX_DISSIMILAR_SEPARATE("MDS"),
    MULTIPLEX_DISSIMILAR_CONNECTED("MDC"),
    MULTIPLEX_DISSIMILAR_FUSED("MDF"),
    MULTIPLEX_FUZZY_SEPARATE("MFS"),
    MULTIPLEX_FUZZY_CONNECTED("MFC"),
    MULTIPLEX_FUZZY_FUSED("MFF");

    companion object {
        fun byAbbreviation(s: String) : Configuration? {
            return values().find { it.short eq s  }
        }
    }

}

enum class Affiliation(override val short: String) : Category {
    CONSOLIDATIVE("CSL"),
    ASSOCIATIVE("ASO"),
    VARIATIVE("VAR"),
    COALESCENT("COA");
}

enum class Extension(override val short: String) : Category {
    DELIMITIVE("DEL"),
    PROXIMAL("PRX"),
    INCIPIENT("ICP"),
    ATTENUATIVE("ATV"),
    GRADUATIVE("GRA"),
    DEPLETIVE("DPL");
}

enum class Perspective(override val short: String) : Category {
    MONADIC("M"),
    POLYADIC("P"),
    NOMIC("N"),
    ABSTRACT("A");
}

enum class Essence(override val short: String) : Category {
    NORMAL("NRM"),
    REPRESENTATIVE("RPV");
}

enum class Context(override val short: String) : Category {
    EXISTENTIAL("EXS"),
    FUNCTIONAL("FNC"),
    REPRESENTATIONAL("RPS"),
    AMALGAMATIVE("AMG");
}

enum class Valence(override val short: String) : Category {
    MONOACTIVE("MNO"),
    PARALLEL("PRL"),
    COROLLARY("CRO"),
    RECIPROCAL("RCP"),
    COMPLEMENTARY("CPL"),
    DUPLICATIVE("DUP"),
    DEMONSTRATIVE("DEM"),
    CONTINGENT("CNG"),
    PARTICIPATIVE("PTI");

    companion object {
        fun byForm(form: Int) = values()[form-1]
    }
}

enum class Phase(override val short: String) : Category {
    CONTEXTUAL("CTX"),
    PUNCTUAL("PCT"),
    ITERATIVE("ITR"),
    REPETITIVE("REP"),
    INTERMITTENT("ITM"),
    RECURRENT("RCT"),
    FREQUENTATIVE("FRE"),
    FRAGMENTATIVE("FRG"),
    FLUCTUATIVE("FLC");

    companion object {
        fun byForm(form: Int) = Valence.values()[form-1]
    }

}


class EffectAndPerson(private val person: String?, private val effect: Effect) : Precision {

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return if (person != null) {
            "$person:${effect.toString(precision, ignoreDefault = false)}"
        } else effect.toString(precision, ignoreDefault = false)
    }

    companion object {
        fun byForm(form: Int) = when (form) {
            1 -> EffectAndPerson("1", Effect.BENEFICIAL)
            2 -> EffectAndPerson("2", Effect.BENEFICIAL)
            3 -> EffectAndPerson("3", Effect.BENEFICIAL)
            4 -> EffectAndPerson("SLF", Effect.BENEFICIAL)
            5 -> EffectAndPerson(null, Effect.UNKNOWN)
            6 -> EffectAndPerson("SLF", Effect.DETRIMENTAL)
            7 -> EffectAndPerson("3", Effect.DETRIMENTAL)
            8 -> EffectAndPerson("2", Effect.DETRIMENTAL)
            9 -> EffectAndPerson("1", Effect.DETRIMENTAL)
            else -> throw(IndexOutOfBoundsException("Invalid vowelform: $form"))
        }
    }

}

enum class Effect(override val short: String) : Category {
    NEUTRAL("NEU"),
    BENEFICIAL("BEN"),
    UNKNOWN("UNK"),
    DETRIMENTAL("DET");
}

enum class Level(override val short: String) : NoDefault {
    EQUATIVE("EQU"),
    SURPASSIVE("SUR"),
    DEFICIENT("DFT"),
    MAXIMAL("MAX"),
    MINIMAL("MIN"),
    SUPERLATIVE("SPL"),
    INFERIOR("IFR"),
    SUPEREQUATIVE("SPQ"),
    SUBEQUATIVE("SUBEQUATIVE");

    companion object {
        fun byForm(form: Int) = Valence.values()[form-1]
    }
}

enum class Aspect(override val short: String, val vn: String) : NoDefault {
    RETROSPECTIVE("RTR", "a"),
    PROSPECTIVE("PRS", "ä"),
    HABITUAL("HAB", "e"),
    PROGRESSIVE("PRG", "ë"),
    IMMINENT("IMM", "i"),
    PRECESSIVE("PCS", "ö"),
    REGULATIVE("REG", "o"),
    SUMMATIVE("SMM", "ü"),
    ANTICIPATORY("ATP", "u"),
    RESUMPTIVE("RSM", "ai"),
    CESSATIVE("CSS", "au"),
    PAUSAL("PAU", "ei"),
    REGRESSIVE("RGR", "eu"),
    PRECLUSIVE("PCL", "ëi"),
    CONTINUATIVE("CNT", "ou"),
    INCESSATIVE("ICS", "oi"),
    EXPERIENTIAL("EXP", "iu"),
    INTERRUPTIVE("IRP", "ui"),
    PREEMPTIVE("PMP", "ia/oä"),
    CLIMACTIC("CLM", "iä/uä"),
    DILATORY("DLT", "ie/oë"),
    TEMPORARY("TMP", "ië/uë"),
    EXPENDITIVE("XPD", "ëu"),
    LIMITATIVE("LIM", "uö/iö"),
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
    MOTIVE("MTV", "öa"),
    SEQUENTIAL("SQN", "oa");

    companion object {
        fun byVowel(vt: String) = values().find { it.vn eq vt }
    }
}

enum class Mood(override val short: String, val cn: String, val cy: String) : Category {
    FACTUAL("FAC", "h/ç", ""),
    SUBJUNCTIVE("SUB", "hl", "x"),
    ASSUMPTIVE("ASM", "hr", "rs"),
    SPECULATIVE("SPC", "hw", "rš"),
    COUNTERFACTIVE("COU", "hm", "rz"),
    HYPOTHETICAL("HYP", "hn", "rž");

    companion object {
        fun byCn(cn: String) = values().find { it.cn eq cn }

        fun byCy(cy: String) = values().find { it.cy eq cy }
    }
}

enum class CaseScope(override val short: String, val cn: String, val cy: String) : Category {
    NATURAL("CCN", "h/ç", ""),
    ANTECEDENT("CCA", "hl", "x"),
    SUBALTERN("CCS", "hr", "rs"),
    QUALIFIER("CCQ", "hw", "rš"),
    PRECEDENT("CCP", "hm", "rz"),
    SUCCESSIVE("CCV", "hn", "rž");

    companion object {
        fun byCn(cn: String) = values().find { it.cn eq cn }

        fun byCy(cy: String) = values().find { it.cy eq cy }
    }
}

enum class Case(override val short: String, val vc: String) : Category {
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

    FUNCTIVE("FUN", "ao"),
    TRANSFORMATIVE("TFM", "ae"),
    CLASSIFICATIVE("CLA", "ea"),
    RESULTATIVE("RSL", "eo"),
    CONSUMPTIVE("CSM", "eë"),
    CONCESSIVE("CON", "öe"),
    AVERSIVE("AVS", "oe"),
    CONVERSIVE("CVS", "öa"),
    SITUATIVE("SIT", "oa"),

    REFERENTIAL("REF", "a'a"),
    ASSIMILATIVE("ASI", "ä'ä"),
    ESSIVE("ESS", "e'e"),
    CORRELATIVE("COR", "ë'ë"),
    COMPOSITIVE("CPS", "i'i"),
    COMITATIVE("COM", "ö'ö"),
    UTILITATIVE("UTL", "o'o"),
    RELATIVE("RLT", "u'u"),

    ACTIVATIVE("ACT", "a'i"),
    DESCRIPTIVE("DSP", "a'u"),
    TERMINATIVE("TRM", "e'i"),
    SELECTIVE("SEL", "e'u"),
    CONFORMATIVE("CFM", "ë'i"),
    DEPENDENT("DEP", "o'u"),
    PREDICATIVE("PRD", "o'i"),
    VOCATIVE("VOC", "u'i"),

    LOCATIVE("LOC", "i'a"),
    ATTENDANT("ATD", "i'ä"),
    ALLATIVE("ALL", "i'e"),
    ABLATIVE("ABL", "i'ë"),
    ORIENTATIVE("ORI", "ë'u"),
    INTERRELATIVE("IRL", "u'ö"),
    INTRATIVE("INV", "u'o"),
    NAVIGATIVE("NAV", "u'a"),

    CONCURSIVE("CNR", "a'o"),
    ASSESSIVE("ASS", "a'e"),
    PERIODIC("PER", "e'a"),
    PROLAPSIVE("PRO", "e'o"),
    PRECURSIVE("PCV", "e'ë"),
    POSTCURSIVE("PCR", "ö'e"),
    ELAPSIVE("ELP", "o'e"),
    PROLIMITIVE("PLM", "o'a");

    companion object {
        fun byVowel(vc: String) = values().find { it.vc eq vc }
    }
}

enum class Illocution(override val short: String) : Category {
    ASSERTIVE("ASR"),
    PERFORMATIVE("PFM");
}

enum class Expectation(override val short: String) : Category {
    COGNITIVE("COG"),
    RESPONSIVE("RSP"),
    EXECUTIVE("EXE");
}

enum class Validation(override val short: String) : NoDefault {
    OBSERVATIONAL("OBS"),
    RECOLLECTIVE("REC"),
    PURPORTIVE("PUP"),
    REPORTIVE("RPR"),
    CONVENTIONAL("CVN"),
    INFERENTIAL("INF"),
    INTUITIVE("ITU"),
    IMAGINARY("IMA");
}

enum class Bias(override val short: String, val cb: String) : NoDefault {
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

    companion object {
        fun byGroup(cb: String) = values().find { it.cb eq cb }
    }
}

enum class Register(override val short: String, val initial: String, val final: String) : NoDefault {
    DISCURSIVE("DSV", "a", "ai"),
    PARENTHETICAL("PNT", "e", "ei"),
    COGITANT("CGT", "o", "oi"),
    EXAMPLIFICATIVE("EXM", "ö", "ëi"),
    SPECIFICATIVE("SPF", "i", "iu"),
    MATHEMATICAL("MTH", "u", "ui"),
    CARRIER_END("CAR", "", "ü");

    companion object {
        fun byVowel(v: String): Pair<Register, Boolean>? {
            return values().find { it.initial eq v }?.let { it to true }
                ?: values().find { it.final eq v }?.to(false)
        }
    }
}

enum class Referent(override val short: String) : NoDefault {
    MONADIC_SPEAKER("1m"),
    MONADIC_ADDRESSEE("2m"),
    POLYADIC_ADDRESSEE("2p"),
    MONADIC_ANIMATE_THIRD_PARTY("ma"),
    POLYADIC_ANIMATE_THIRD_PARTY("pa"),
    MONADIC_INANIMATE_THIRD_PARTY("mi"),
    POLYADIC_INANIMATE_THIRD_PARTY("pi"),
    MIXED_THIRD_PARTY("Mx"),
    OBVIATIVE("Obv"),
    PROVISIONAL("PVS");
}

fun parsePraShortcut(c: String, v: String, precision: Int): String? {
    val (series, form) = seriesAndForm(v)
    if (series != 3) return null
    val case = when (form) {
        1 -> Case.POSSESSIVE
        2 -> Case.PROPRIETIVE
        3 -> Case.GENITIVE
        4 -> Case.ATTRIBUTIVE
        5 -> Case.PRODUCTIVE
        6 -> Case.INTERPRETIVE
        7 -> Case.ORIGINATIVE
        8 -> Case.COMITATIVE
        9 -> Case.CORRELATIVE
        else -> return null
    }
    val ref = parsePersonalReference(c)?.toString(precision) ?: return null
    return "($ref-${case.toString(precision)})"
}