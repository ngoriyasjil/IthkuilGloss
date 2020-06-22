package io.github.syst3ms.tnil

import java.lang.IllegalStateException

val VOWEL_FORM = listOf(
    "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
    "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "ia/öa", "iä/uä", "ie/oë", "ië/uë", "ëu", "uö/iö", "uo/io", "ue/eö", "ua/aö",
    "ao", "ae", "ea", "eo", "eë", "öe", "oe", "öa", "oa",
    "a'a", "ä'ä", "e'e", "ë'ë", "i'i", "ö'ö", "o'o", "ü'ü", "u'u",
    "a'i", "a'u", "e'i", "e'u", "ë'i", "o'u", "o'i", "i'u", "u'i",
    "i'a", "i'ä", "i'e", "i'ë", "ë'u", "u'ö", "u'o", "u'e", "u'a",
    "a'o", "a'e", "e'a", "e'o", "e'ë", "ö'e", "o'e", "ö'a", "o'a",
    "awo", "awe", "ewa", "ewo", "ewë", "öwe", "owe", "öwa", "owa"
)
val flatVowelForm = VOWEL_FORM.flatMap { it.split("/") }
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
        "hw", "çw", "hl", "hr",
        "hlw", "hly", "hm", "hn",
        "hmw", "hnw", "hmy", "hny"
)
val INVALID_LEXICAL_CONSONANTS = listOf("ļ", "ç", "çç", "ř", "h", "w", "y")
val affixVowel = listOf(
    "üa", "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
    "üe", "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "üo", "ia/oä", "iä/uä", "ie/oë", "ië/uë", "ëu", "uö/iö", "uo/io", "ue/eö", "ua/aö",
    "üö", "ao", "ae", "ea", "eo", "eë", "öe", "oe", "öa", "oa",
    "üwö", "awo", "awe", "ewa", "ewo", "ewë", "öwe", "owe", "öwa", "owa"
)
val combinationPRASpecification = listOf("bz", "gz", "bž", "gž")
val affixualScopingConsonants = listOf("w", "y", "h", "'w", "'y", "'h")
val animateReferentDescriptions = listOf(
        listOf("monadic speaker (1m), \"I\"", "polyadic speaker (1p), \"we\"", "oneself in a hypothetical/timeless context", "all that I am, that makes me myself"),
        listOf("monadic addressee (2m), \"you (sg.)\"", "polyadic addressee (2p) \"you (pl.)\"", "the addressee in a hypothetical/timeless context", "all that you are, that makes you yourself"),
        listOf("monadic animate 3rd party (ma), \"he/she/they\"", "polyadic animate 3rd party (pa), \"they (pl.)\"", "impersonal animate (IPa), \"one\"", "all that (s)he/they are")
)
val inanimateReferentDescriptions = listOf(
        listOf("monadic inanimate 3rd party (mi), \"it\"", "polyadic inanimate 3rd party (pi), \"them/those\"", "impersonal inanimate (IPi), \"something\"", "all that it/they are"),
        listOf("monadic obviative (mObv)", "polyadic obviative (pObv)", "Nai, \"it\" as a generic concept", "Aai, \"it\" as an abstract referent"),
        listOf("monadic mixed animate+inanimate (mMx)", "polyadic mixed animate+inanimate (pMx)", "impersonal mixed animate+inanimate (IPx)", "everything and everyone, all about the world")
)

interface Precision {
    fun toString(precision: Int, ignoreDefault: Boolean = false): String
}

enum class Designation(val short: String) : Precision {
    INFORMAL("IFL"),
    FORMAL("FML");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Incorporation(val short: String) : Precision {
    TYPE_ONE("T1"),
    TYPE_TWO("T2");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Version(val short: String) : Precision {
    PROCESSUAL("PRC"),
    COMPLETIVE("CPT");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Relation(val short: String) : Precision {
    UNFRAMED("UNF"),
    FRAMED("FRA");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Stem(val short: String) : Precision {
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

enum class Specification(val short: String) : Precision {
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
enum class Function(val short: String) : Precision {
    STATIVE("STA"),
    DYNAMIC("DYN");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Uniplex(val short: String) : Precision {
    SPECIFIC("UXS"),
    POTENTIAL("UPX");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Similarity(val short: String) : Precision {
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

enum class Connectedness(val short: String) : Precision {
    SEPARATE("SEP"),
    CONNECTED("CND"),
    FUSED("FSD");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Affiliation(val short: String) : Precision {
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

enum class Extension(val short: String) : Precision {
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

enum class Perspective(val short: String) : Precision {
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

enum class Essence(val short: String) : Precision {
    NORMAL("NRM"),
    REPRESENTATIVE("RPV");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Context(val short: String) : Precision {
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

enum class Valence(val short: String) : Precision {
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

enum class Phase(val short: String, val vp: String) : Precision {
    CONTEXTUAL("CTX", "wa"),
    PUNCTUAL("PCT", "wä"),
    ITERATIVE("ITR", "we"),
    REPETITIVE("REP", "wë"),
    INTERMITTENT("ITM", "wi"),
    RECURRENT("RCT", "wö"),
    FREQUENTATIVE("FRE", "wo"),
    FRAGMENTATIVE("FRG", "wü"),
    FLUCTUATIVE("FLC", "wu");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byVowel(vp: String) = values().find { it.vp eq vp }
    }
}

enum class Effect(val short: String) : Precision {
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

enum class Level(val short: String) : Precision {
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

enum class Aspect(val short: String, val vt: String) : Precision {
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

enum class Mood(val short: String, val cn: String, val cy: String) : Precision {
    FACTUAL("FAC", "h/ç", ""),
    SUBJUNCTIVE("SUB", "hl", "x"),
    ASSUMPTIVE("ASM", "hr", "rz"),
    SPECULATIVE("SPC", "hw", "rž"),
    COUNTERFACTIVE("COU", "hm", "lz"),
    HYPOTHETICAL("HYP", "hn", "lž");

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

enum class CaseScope(val short: String, val cn: String, val cy: String) : Precision {
    CCH("CCh", "h/ç", ""),
    CCL("CCl", "hl", "x"),
    CCR("CCr", "hr", "rz"),
    CCW("CCw", "hw", "rž"),
    CCM("CCm", "hm", "lz"),
    CCn("CCn", "hn", "lž");

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

enum class Case(val short: String, val vc: String, val vfShort: String? = null) : Precision {
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

enum class Illocution(val short: String) : Precision {
    ASSERTIVE("ASR"),
    PERFORMATIVE("PFM");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Expectation(val short: String) : Precision {
    COGNITIVE("COG"),
    RESPONSIVE("RSP"),
    EXECUTIVE("EXE");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Validation(val short: String) : Precision {
    OBSERVATIONAL("OBS"),
    RECOLLECTIVE("REC"),
    REPORTIVE("RPR"),
    INFERENTIAL("INF"),
    INTUITIVE("ITU"),
    IMAGINARY("IMA");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Bias(val short: String, val cb: String) : Precision {
    DOLOROUS("DOL", "řřx"),
    SKEPTICAL("SKP", "rnž"),
    IMPATIENT("IPT", "zzv"),
    REVELATIVE("RVL", "mmļ"),
    TREPIDATIVE("TRP", "llč"),
    REPULSIVE("RPU", "šštļ"),
    DESPERATIVE("DES", "mřř"),
    DISAPPROBATIVE("DPB", "ffx"),
    PROSAIC("PSC", "zzt"),
    COMEDIC("CMD", "pļļ"),
    PROPOSITIVE("PPV", "sl"),
    SUGGESTIVE("SGS", "ltç"),
    DIFFIDENT("DFD", "cč"),
    SELECTIVE("SEL", "rrm"),
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
    FASCINATIVE("FSC", "zzj"),
    INFATUATIVE("IFT", "vvr"),
    EUPHORIC("EUH", "gzz"),
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
    DELECTATIVE("DLC", "žž"),
    ATTENTIVE("ATE", "ňj"),
    RENUNCIATIVE("RNC", "mzt"),
    MANDATORY("MND", "msk"),
    EXIGENT("EXG", "rrs"),
    INSIPID("ISP", "lçp"),
    ADMISSIVE("ADM", "lļ"),
    APPREHENSIVE("APH", "vvz"),
    OPTIMAL("OPT", "ççk"),
    ASSERTIVE("ASV", "rrj"),
    IMPLICATIVE("IPL", "vll"),
    ACCIDENTAL("ACC", "lf"),
    ANTICIPATIVE("ANP", "lst"),
    ARCHETYPAL("ACH", "mçt"),
    VEXATIVE("VEX", "ksk"),
    CORRUPTIVE("CRP", "gžž"),
    DEJECTIVE("DEJ", "zzg");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byGroup(cb: String) = values().find { it.cb eq cb }
    }
}

enum class Register(val short: String, val initial: String, val final: String) : Precision {
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

enum class Referent(val short: String) : Precision {
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

fun parseCd(c: String) : Pair<List<Precision>, Boolean> {
    val i = CD_CONSONANTS.indexOf(c.defaultForm())
    return listOf<Precision>(Designation.values()[(i % 8) % 2], Incorporation.values()[(i % 8)/2 % 2], Version.values()[i%8 / 4]) to (i >= 8)
}

fun parseValenceContext(v: String): List<Precision>? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1)
        return null
    return listOf(Context.values()[i / 9], Valence.values()[i % 9])
}

fun parsePhaseContext(v: String): List<Precision>? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1)
        return null
    return listOf(Context.values()[i / 9], Phase.values()[i % 9])
}

fun parseLevelContext(v: String): List<Precision>? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1)
        return null
    return listOf(Context.values()[i / 9], Level.values()[i % 9])
}

fun parseEffectContext(v: String, precision: Int, ignoreDefault: Boolean): String? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1)
        return null
    val ben = Effect.BENEFICIAL.toString(precision)
    val det = Effect.DETRIMENTAL.toString(precision)
    val unk = Effect.UNKNOWN.toString(precision)
    return Context.values()[i / 9].toString(precision, ignoreDefault).plusSeparator(sep = "/") + when (i % 9) {
        0 -> "1/$ben"
        1 -> "2/$ben"
        2 -> "3/$ben"
        3 -> "all/$ben"
        4 -> unk
        5 -> "all/$det"
        6 -> "3/$det"
        7 -> "2/$det"
        8 -> "1/$det"
        else -> throw IllegalStateException()
    }
}

fun parseVk(s: String) : List<Precision>? = when(s.defaultForm()) {
    "a" -> listOf(Illocution.ASSERTIVE, Expectation.COGNITIVE, Validation.OBSERVATIONAL)
    "e" -> listOf(Illocution.ASSERTIVE, Expectation.COGNITIVE, Validation.RECOLLECTIVE)
    "i" -> listOf(Illocution.ASSERTIVE, Expectation.COGNITIVE, Validation.REPORTIVE)
    "o" -> listOf(Illocution.ASSERTIVE, Expectation.COGNITIVE, Validation.INFERENTIAL)
    "u" -> listOf(Illocution.ASSERTIVE, Expectation.COGNITIVE, Validation.INTUITIVE)
    "ä" -> listOf(Illocution.ASSERTIVE, Expectation.COGNITIVE, Validation.IMAGINARY)
    "ai" -> listOf(Illocution.ASSERTIVE, Expectation.RESPONSIVE, Validation.OBSERVATIONAL)
    "ei" -> listOf(Illocution.ASSERTIVE, Expectation.RESPONSIVE, Validation.RECOLLECTIVE)
    "ëi" -> listOf(Illocution.ASSERTIVE, Expectation.RESPONSIVE, Validation.REPORTIVE)
    "oi" -> listOf(Illocution.ASSERTIVE, Expectation.RESPONSIVE, Validation.INFERENTIAL)
    "ui" -> listOf(Illocution.ASSERTIVE, Expectation.RESPONSIVE, Validation.INTUITIVE)
    "ae" -> listOf(Illocution.ASSERTIVE, Expectation.RESPONSIVE, Validation.IMAGINARY)
    "au" -> listOf(Illocution.ASSERTIVE, Expectation.EXECUTIVE, Validation.OBSERVATIONAL)
    "eu" -> listOf(Illocution.ASSERTIVE, Expectation.EXECUTIVE, Validation.RECOLLECTIVE)
    "ëu" -> listOf(Illocution.ASSERTIVE, Expectation.EXECUTIVE, Validation.REPORTIVE)
    "ou" -> listOf(Illocution.ASSERTIVE, Expectation.EXECUTIVE, Validation.INFERENTIAL)
    "iu" -> listOf(Illocution.ASSERTIVE, Expectation.EXECUTIVE, Validation.INTUITIVE)
    "ao" -> listOf(Illocution.ASSERTIVE, Expectation.EXECUTIVE, Validation.IMAGINARY)
    "ë" -> listOf(Illocution.PERFORMATIVE, Expectation.COGNITIVE, Validation.OBSERVATIONAL)
    "ö" -> listOf(Illocution.PERFORMATIVE, Expectation.RESPONSIVE, Validation.OBSERVATIONAL)
    "ü" -> listOf(Illocution.PERFORMATIVE, Expectation.EXECUTIVE, Validation.OBSERVATIONAL)
    else -> null
}

fun parseVvSimple(s: String) : List<Precision>? = when {
    "a" eq s  -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.UNFRAMED)
    "ä" eq s -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.FRAMED)
    "e" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.UNFRAMED)
    "i" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.FRAMED)
    "u" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.UNFRAMED)
    "ü" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.FRAMED)
    "o" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.UNFRAMED)
    "ö" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.FRAMED)
    else -> null
}

fun parseVvComplex(s: String) : List<Precision>? = when {
    "a" eq s  -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_ONE)
    "ä" eq s -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_ONE)
    "e" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_ONE)
    "i" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_ONE)
    "u" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_ONE)
    "ü" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_ONE)
    "o" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_ONE)
    "ö" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_ONE)
    "ai" eq s  -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_TWO)
    "au" eq s -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_TWO)
    "ei" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_TWO)
    "eu" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_TWO)
    "ui" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_TWO)
    "iu" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_TWO)
    "oi" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_TWO)
    "ou" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_TWO)
    "ia/oä" eq s  -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_THREE)
    "iä/uä" eq s -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_THREE)
    "ie/oë" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_THREE)
    "ië/uë" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_THREE)
    "ua/aö" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_THREE)
    "ue/eö" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_THREE)
    "uo/io" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_THREE)
    "uö/iö" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_THREE)
    "ao" eq s  -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_ZERO)
    "ae" eq s -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_ZERO)
    "ea" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_ZERO)
    "eo" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_ZERO)
    "oa" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.UNFRAMED, Stem.STEM_ZERO)
    "öa" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.FRAMED, Stem.STEM_ZERO)
    "oe" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.UNFRAMED, Stem.STEM_ZERO)
    "öe" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.FRAMED, Stem.STEM_ZERO)
    else -> null
}

fun parseVr(s: String): List<Precision>? = when {
    "a" eq s -> listOf(Specification.BASIC, Stem.STEM_ONE, Function.STATIVE)
    "ä" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ONE, Function.STATIVE)
    "e" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ONE, Function.STATIVE)
    "i" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ONE, Function.STATIVE)
    "ai" eq s -> listOf(Specification.BASIC, Stem.STEM_TWO, Function.STATIVE)
    "au" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_TWO, Function.STATIVE)
    "ei" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_TWO, Function.STATIVE)
    "eu" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_TWO, Function.STATIVE)
    "ia/oä" eq s -> listOf(Specification.BASIC, Stem.STEM_THREE, Function.STATIVE)
    "iä/uä" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_THREE, Function.STATIVE)
    "ie/oë" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_THREE, Function.STATIVE)
    "ië/uë" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_THREE, Function.STATIVE)
    "ao" eq s -> listOf(Specification.BASIC, Stem.STEM_ZERO, Function.STATIVE)
    "ae" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ZERO, Function.STATIVE)
    "ea" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ZERO, Function.STATIVE)
    "eo" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ZERO, Function.STATIVE)
    "u" eq s -> listOf(Specification.BASIC, Stem.STEM_ONE, Function.DYNAMIC)
    "ü" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ONE, Function.DYNAMIC)
    "o" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ONE, Function.DYNAMIC)
    "ö" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ONE, Function.DYNAMIC)
    "ui" eq s -> listOf(Specification.BASIC, Stem.STEM_TWO, Function.DYNAMIC)
    "iu" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_TWO, Function.DYNAMIC)
    "oi" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_TWO, Function.DYNAMIC)
    "ou" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_TWO, Function.DYNAMIC)
    "ua/aö" eq s -> listOf(Specification.BASIC, Stem.STEM_THREE, Function.DYNAMIC)
    "ue/eö" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_THREE, Function.DYNAMIC)
    "uo/io" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_THREE, Function.DYNAMIC)
    "uö/iö" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_THREE, Function.DYNAMIC)
    "oa" eq s -> listOf(Specification.BASIC, Stem.STEM_ZERO, Function.DYNAMIC)
    "öa" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ZERO, Function.DYNAMIC)
    "oe" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ZERO, Function.DYNAMIC)
    "öe" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ZERO, Function.DYNAMIC)
    else -> null
}


fun parsePersonalReference(s: String, final: Boolean = false): List<Precision>? = when (val r = s.defaultForm()) {
    "l" -> listOf(Referent.MONADIC_SPEAKER, Effect.NEUTRAL)
    "r" -> listOf(Referent.MONADIC_SPEAKER, Effect.BENEFICIAL)
    "ř" -> listOf(Referent.MONADIC_SPEAKER, Effect.DETRIMENTAL)
    "s" -> listOf(Referent.MONADIC_ADDRESSEE, Effect.NEUTRAL)
    "š" -> listOf(Referent.MONADIC_ADDRESSEE, Effect.BENEFICIAL)
    "ž" -> listOf(Referent.MONADIC_ADDRESSEE, Effect.DETRIMENTAL)
    "n" -> listOf(Referent.POLYADIC_ADDRESSEE, Effect.NEUTRAL)
    "t" -> listOf(Referent.POLYADIC_ADDRESSEE, Effect.BENEFICIAL)
    "d" -> listOf(Referent.POLYADIC_ADDRESSEE, Effect.DETRIMENTAL)
    "m" -> listOf(Referent.MONADIC_ANIMATE_THIRD_PARTY, Effect.NEUTRAL)
    "p" -> listOf(Referent.MONADIC_ANIMATE_THIRD_PARTY, Effect.BENEFICIAL)
    "b" -> listOf(Referent.MONADIC_ANIMATE_THIRD_PARTY, Effect.DETRIMENTAL)
    "ň" -> listOf(Referent.POLYADIC_ANIMATE_THIRD_PARTY, Effect.NEUTRAL)
    "k" -> listOf(Referent.POLYADIC_ANIMATE_THIRD_PARTY, Effect.BENEFICIAL)
    "g" -> listOf(Referent.POLYADIC_ANIMATE_THIRD_PARTY, Effect.DETRIMENTAL)
    "z" -> listOf(Referent.MONADIC_INANIMATE_THIRD_PARTY, Effect.NEUTRAL)
    "ţ" -> listOf(Referent.MONADIC_INANIMATE_THIRD_PARTY, Effect.BENEFICIAL)
    "ḑ" -> listOf(Referent.MONADIC_INANIMATE_THIRD_PARTY, Effect.DETRIMENTAL)
    "ļ" -> listOf(Referent.POLYADIC_INANIMATE_THIRD_PARTY, Effect.NEUTRAL)
    "f" -> listOf(Referent.POLYADIC_INANIMATE_THIRD_PARTY, Effect.BENEFICIAL)
    "v" -> listOf(Referent.POLYADIC_INANIMATE_THIRD_PARTY, Effect.DETRIMENTAL)
    "x" -> listOf(Referent.MIXED_THIRD_PARTY, Effect.NEUTRAL)
    "c" -> listOf(Referent.MIXED_THIRD_PARTY, Effect.BENEFICIAL)
    "ż" -> listOf(Referent.MIXED_THIRD_PARTY, Effect.DETRIMENTAL)
    "th" -> listOf(Referent.OBVIATIVE, Effect.NEUTRAL)
    "ph" -> listOf(Referent.OBVIATIVE, Effect.BENEFICIAL)
    "kh" -> listOf(Referent.OBVIATIVE, Effect.DETRIMENTAL)
    "tç" -> listOf(Referent.ANIMATE_IMPERSONAL, Effect.NEUTRAL)
    "pç" -> listOf(Referent.ANIMATE_IMPERSONAL, Effect.BENEFICIAL)
    "kç" -> listOf(Referent.ANIMATE_IMPERSONAL, Effect.DETRIMENTAL)
    "çn", "nç" -> if (final || r == "çn") listOf<Precision>(Referent.INANIMATE_IMPERSONAL, Effect.NEUTRAL) else null
    "çm", "mç" -> if (final || r == "çm") listOf<Precision>(Referent.INANIMATE_IMPERSONAL, Effect.BENEFICIAL) else null
    "ňç" -> listOf(Referent.INANIMATE_IMPERSONAL, Effect.DETRIMENTAL)
    "çl", "lç" -> if (final || r == "çl") listOf<Precision>(Referent.NOMIC_REFERENT, Effect.NEUTRAL) else null
    "çr", "rç" -> if (final || r == "çr") listOf<Precision>(Referent.NOMIC_REFERENT, Effect.BENEFICIAL) else null
    "çř", "řç" -> if (final || r == "çř") listOf<Precision>(Referent.NOMIC_REFERENT, Effect.DETRIMENTAL) else null
    "rr" -> listOf(Referent.ABSTRACT_REFERENT, Effect.NEUTRAL)
    "č" -> listOf(Referent.ABSTRACT_REFERENT, Effect.BENEFICIAL)
    "j" -> listOf(Referent.ABSTRACT_REFERENT, Effect.DETRIMENTAL)
    else -> null
}

fun String.isGlottalCa(): Boolean {
    if (startsWith("'")) {
        return true
    } else if (length >= 3) {
        val pre = take(2)
        if (pre[0] == pre[1]) {
            if (pre[0] == 'r' ||
                    pre[0] == 'l' ||
                    pre[0].toString() in NASALS ||
                    pre[0].toString() in FRICATIVES ||
                    pre[0].toString() in AFFRICATES) {
                return true
            } else if (pre[0].toString() in STOPS && this[2].toString() in listOf("l", "r", "ř", "w", "y")) {
                return true
            }
        }
    }
    return false
}

fun parseCa(s: String) : List<Precision>? {
    val elements = arrayListOf<Precision>()
    var original = s.defaultForm()
    if (original.isEmpty())
        return null
    when (original) {
        "l" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.MONADIC, Essence.NORMAL)
        "ř" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.MONADIC, Essence.REPRESENTATIVE)
        "r" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.POLYADIC, Essence.NORMAL)
        "tļ" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.POLYADIC, Essence.REPRESENTATIVE)
        "v" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.NOMIC, Essence.NORMAL)
        "lm" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.NOMIC, Essence.REPRESENTATIVE)
        "ẓ" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.ABSTRACT, Essence.NORMAL)
        "ln" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.ABSTRACT, Essence.REPRESENTATIVE)
        "d" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.ASSOCIATIVE, Perspective.MONADIC, Essence.NORMAL)
        "g" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.COALESCENT, Perspective.MONADIC, Essence.NORMAL)
        "b" -> return listOf(Uniplex.SPECIFIC, Extension.DELIMITIVE, Affiliation.VARIATIVE, Perspective.MONADIC, Essence.NORMAL)
        "lţ" -> return listOf(Uniplex.POTENTIAL, Extension.DELIMITIVE, Affiliation.CONSOLIDATIVE, Perspective.MONADIC, Essence.NORMAL)
    }
    original = original.replace("mz", "mm")
        .replace("nd", "nn")
        .replace("lf", "pp")
        .replace("lš", "kk")
        .replace("ls", "tt")
        .replace("j", "čy")
        .replace("nž", "çy")
        .replace("nz", "ňy")
        .replace("v(?=.)".toRegex(), "nf")
        .replace("fš(?=.)".toRegex(), "kf")
        .replace("fs(?=.)".toRegex(), "tf")
        .replace("ng", "ňk")
        .replace("mb", "np")
        .replace("ḑ", "tţ")
        .replace("č", "tš")
        .replace("c", "ts")
        .replace("rç", "ţç")
        .replace("rs", "ţţ")
        .replace("rţ", "ţf")
        .replace("ž", "ţš")
        .replace("z", "ţs")
    val a = when {
        original.endsWith("ļ") -> {
            elements.add(Perspective.MONADIC)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        original.endsWith("r") || original.endsWith("v") -> {
            if (original.endsWith("v") && !(original.startsWith("ř") || original.getOrNull(original.length - 2) in listOf('b', 'd', 'g')))
                return null
            elements.add(Perspective.POLYADIC)
            elements.add(Essence.NORMAL)
            1
        }
        original.endsWith("l") -> {
            if (original.length == 1)
                return null
            elements.add(Perspective.POLYADIC)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        original.endsWith("w") -> {
            elements.add(Perspective.NOMIC)
            elements.add(Essence.NORMAL)
            1
        }
        original.endsWith("m") || original.endsWith("h") -> {
            if (original.endsWith("h") &&
                    (original.length < 3
                            || original[original.lastIndex - 1].toString() !in STOPS
                            || original[original.lastIndex - 2].toString() !in FRICATIVES)) {
                return null
            }
            elements.add(Perspective.NOMIC)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        original.endsWith("y") -> {
            if (original.length == 1)
                return null
            elements.add(Perspective.ABSTRACT)
            elements.add(Essence.NORMAL)
            1
        }
        original.endsWith("n") || original.endsWith("ç") -> {
            if (original.endsWith("ç") &&
                    (original.length < 3
                            || original[original.lastIndex - 1].toString() !in STOPS
                            || original[original.lastIndex - 2].toString() !in FRICATIVES)) {
                return null
            }
            elements.add(Perspective.ABSTRACT)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        else -> {
            elements.add(Perspective.MONADIC)
            elements.add(Essence.NORMAL)
            0
        }
    }
    original = original.dropLast(a)
    when (original) {
        "d" -> {
            elements.add(0, Affiliation.ASSOCIATIVE)
            return elements
        }
        "g" -> {
            elements.add(0, Affiliation.COALESCENT)
            return elements
        }
        "b" -> {
            elements.add(0, Affiliation.VARIATIVE)
            return elements
        }
    }
    val b = when (original.lastOrNull()) { // Dirty hack exploiting the fact that in Kotlin, 'void' functions return a singleton object called Unit
        't' -> if (original.length == 1 || original matches "[rř][ptk]".toRegex()) {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            null
        } else {
            elements.add(0, Affiliation.ASSOCIATIVE)
        }
        'k' -> if (original.length == 1 || original matches "[rř][ptk]".toRegex()) {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            null
        } else {
            elements.add(0, Affiliation.COALESCENT)
        }
        'p' -> if (original.length == 1 || original matches "[rř][ptk]".toRegex()) {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            null
        } else {
            elements.add(0, Affiliation.VARIATIVE)
        }
        else -> {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            null
        }
    }
    if (b == Unit)
        original = original.dropLast(1)
    val c = when (original.lastOrNull()) {
        's' -> elements.add(0, Extension.PROXIMAL)
        'š' -> elements.add(0, Extension.INCIPIENT)
        'f' -> elements.add(0, Extension.ATTENUATIVE)
        'ţ' -> elements.add(0, Extension.GRADUATIVE)
        'ç' -> elements.add(0, Extension.DEPLETIVE)
        else -> {
            elements.add(0, Extension.DELIMITIVE)
            null
        }
    }
    if (c == Unit)
        original = original.dropLast(1)
    when (original) {
        "ţ" -> elements.add(0, Uniplex.POTENTIAL)
        "rt" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.DUPLEX_SIMILAR)
        }
        "rk" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.DUPLEX_SIMILAR)
        }
        "rp" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.DUPLEX_SIMILAR)
        }
        "rn" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.DUPLEX_DISSIMILAR)
        }
        "rň" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.DUPLEX_DISSIMILAR)
        }
        "rm" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.DUPLEX_DISSIMILAR)
        }
        "řt" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.DUPLEX_FUZZY)
        }
        "řk" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.DUPLEX_FUZZY)
        }
        "řp" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.DUPLEX_FUZZY)
        }
        "t" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.MULTIPLEX_SIMILAR)
        }
        "k" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.MULTIPLEX_SIMILAR)
        }
        "p" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.MULTIPLEX_SIMILAR)
        }
        "n" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.MULTIPLEX_DISSIMILAR)
        }
        "ň" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.MULTIPLEX_DISSIMILAR)
        }
        "m" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.MULTIPLEX_DISSIMILAR)
        }
        "lt" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
        }
        "lk" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
        }
        "lp" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
        }
        "" -> elements.add(0, Uniplex.SPECIFIC)
        else -> return null
    }
    return elements
}