package io.github.syst3ms.tnil

val VOWEL_FORM = listOf(
    "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
    "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "ia/öa", "ua/aö", "ie/öe", "ue/eö", "ëu", "uo/iö", "io/uö", "uë/iä", "ië/uä",
    "ao", "ae", "ea", "eo", "oë", "oe", "oa", "eä", "oä",
    "aya", "awa", "eya", "ewa", "iwa", "owa", "oya", "öwa", "uya",
    "aye", "awe", "eye", "ewe", "iwe", "owe", "oye", "öwe", "uye",
    "ayo", "awo", "eyo", "ewo", "iwo", "owo", "oyo", "öwo", "uyo",
    "ayë", "awë", "eyë", "ewë", "iwë", "owë", "oyë", "öwë", "uyë",
    "ayu", "awi", "eyu", "ewi", "iwi", "owi", "oyu", "öwi", "uyu",
    "ayö", "awö", "eyö", "ewö", "iwö", "owö", "oyö", "öwö", "uyö"
)
val CONSONANTS = listOf(
    "p", "b", "t", "d", "k", "g", "'", "f", "v", "ţ", "ḑ", "s", "z", "š", "ž", "ç", "x", "h", "ļ",
    "c", "ẓ", "č", "j", "m", "n", "ň", "r", "l", "w", "y", "ř"
)
val MODULAR_CONSONANTS = listOf("w", "y", "ç", "'", "h", "hl", "hr", "hw", "hm", "")
val CD_CONSONANTS = listOf("h", "ř", "w", "y", "hl", "hr", "hw", "hy")
val INVALID_LEXICAL_CONSONANTS = listOf("ļ", "ç", "çç", "ř", "h", "w", "y")
val affixVowel = listOf(
    "ae", "a", "ä", "e", "ë", "i", "ö", "o", "ü", "u",
    "ea", "ai", "au", "ei", "eu", "ëi", "ou", "oi", "iu", "ui",
    "oa", "ia/öa", "ua/aö", "ie/öe", "ue/eö", "ëu", "uo/iö", "io/uö", "uë/iä", "ië/uä"
)
val combinationPRASpecification = listOf("tm", "sn", "km", "šn")
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
    BENEFICIAL("BEN"),
    DETRIMENTAL("DET"),
    NEUTRAL("NEU"),
    UNKNOWN("UNK");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
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

enum class Aspect(val short: String, val t1: String, val t2: String) : Precision {
    RETROSPECTIVE("RTR", "a", "ya"),
    PROSPECTIVE("PRS", "ä", "yä"),
    HABITUAL("HAB", "e", "ye"),
    PROGRESSIVE("PRG", "ë", "yë"),
    IMMINENT("IMM", "i", "yi"),
    PRECESSIVE("PCS", "ö", "yö"),
    REGULATIVE("REG", "o", "yo"),
    ANTECENDENT("ATC", "ü", "yü"),
    ANTICIPATORY("ATP", "u", "yu"),
    RESUMPTIVE("RSM", "ai", "yai"),
    CESSATIVE("CSS", "au", "yau"),
    PAUSAL("PAU", "ei", "yei"),
    REGRESSIVE("RGR", "eu", "yeu"),
    PRECLUSIVE("PCL", "ëi", "yëi"),
    CONTINUATIVE("CNT", "ou", "you"),
    INCESSATIVE("ICS", "oi", "yoi"),
    SUMMATIVE("SMM", "iu", "yiu"),
    INTERRUPTIVE("IRP", "ui", "yui"),
    PREEMPTIVE("PMP", "ia/öa", "yöa"),
    CLIMACTIC("CLM", "ua/aö", "yua"),
    DILATORY("DLT", "ie/öe", "yöe"),
    TEMPORARY("TMP", "ue/eö", "yue"),
    MOTIVE("MTV", "ëu", "yëu"),
    SEQUENTIAL("SQN", "uo/iö", "yuo"),
    EXPEDITIVE("EPD", "io/uö", "yuö"),
    PROTRACTIVE("PTC", "uë/iä", "yuë"),
    PREPARATORY("PPR", "ië/uä", "yuä"),
    DISCLUSIVE("DCL", "ao", "yao"),
    CONCLUSIVE("CCL", "ae", "yae"),
    CULMINATIVE("CUL", "ea", "yea"),
    INTERMEDIATIVE("IMD", "eo", "yeo"),
    TARDATIVE("TRD", "oë", "yoë"),
    TRANSITIONAL("TNS", "oe", "yoe"),
    INTERCOMMUTATIVE("ITC", "oa", "yoa"),
    EXPENDITIVE("XPD", "eä", "yeä"),
    LIMITATIVE("LIM", "oä", "yoä");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byT1(t1: String) = values().find { it.t1 eq t1 }

        fun byT2(t2: String) = values().find { it.t2 eq t2 }
    }
}

enum class Mood(val short: String, val cm: String, val vm: String) : Precision {
    FACTUAL("FAC", "h/ç", "hai"),
    SUBJUNCTIVE("SUB", "hl", "he"),
    ASSUMPTIVE("ASM", "hr", "hi"),
    SPECULATIVE("SPC", "hw", "hö"),
    COUNTERFACTIVE("COU", "hm", "ho"),
    HYPOTHETICAL("HYP", "hn", "hui");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byGroup(cm: String) = values().find { it.cm eq cm }

        fun byVowel(vm: String) = values().find { it.vm eq vm }
    }
}

enum class CaseScope(val short: String, val cc: String) : Precision {
    CCH("CCh", "h/ç"),
    CCL("CCl", "hl"),
    CCR("CCr", "hr"),
    CCW("CCw", "hw"),
    CCM("CCm", "hm"),
    CCn("CCn", "hn");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byGroup(cc: String) = values().find { it.cc eq cc }
    }
}

enum class Case(val short: String, val vc: String) : Precision {
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
    APPLICATIVE("APL", "ia/öa"),
    PURPOSIVE("PUR", "ua/aö"),
    TRANSMISSIVE("TRA", "ie/öe"),
    DEFERENTIAL("DFR", "ue/eö"),
    CONTRASTIVE("CRS", "ëu"),
    TRANSPOSITIVE("TSP", "uo/iö"),
    COMMUTATIVE("CMM", "io/uö"),
    COMPARATIVE("CMP", "uë/iä"),
    CONSIDERATIVE("CSD", "ië/uä"),
    CONCESSIVE("CON", "ao"),
    AVERSIVE("AVS", "ae"),
    CONVERSIVE("CVS", "ea"),
    SITUATIVE("SIT", "eo"),
    FUNCTIVE("FUN", "oë"),
    TRANSFORMATIVE("TFM", "oe"),
    CLASSIFICATIVE("CLA", "oa"),
    CONSUMPTIVE("CSM", "eä"),
    RESULTATIVE("RSL", "oä"),
    LOCATIVE("LOC", "aya"),
    ATTENDANT("ATD", "awa"),
    ALLATIVE("ALL", "eya"),
    ABLATIVE("ABL", "ewa"),
    ORIENTATIVE("ORI", "iwa"),
    INTERRELATIVE("IRL", "owa"),
    INTRATIVE("INV", "oya"),
    NAVIGATIVE("NAV", "uya"),
    ASSESSIVE("ASS", "aye"),
    CONCURSIVE("CNR", "awe"),
    PERIODIC("PER", "eye"),
    PROLAPSIVE("PRO", "ewe"),
    PRECURSIVE("PCV", "iwe"),
    POSTCURSIVE("PCR", "owe"),
    ELAPSIVE("ELP", "oye"),
    PROLIMITIVE("PLM", "uye"),
    REFERENTIAL("REF", "ayo"),
    CORRELATIVE("COR", "awo"),
    COMPOSITIVE("CPS", "eyo"),
    DEPENDENT("DEP", "ewo"),
    PREDICATIVE("PRD", "iwo"),
    ESSIVE("ESS", "owo"),
    ASSIMILATIVE("ASI", "oyo"),
    CONFORMATIVE("CFM", "uyo"),
    ACTIVATIVE("ACT", "ayë"),
    SELECTIVE("SEL", "awë"),
    COMITATIVE("COM", "eyë"),
    UTILITATIVE("UTL", "ewë"),
    DESCRIPTIVE("DSC", "iwë"),
    RELATIVE("RLT", "owë"),
    TERMINATIVE("TRM", "oyë"),
    VOCATIVE("VOC", "uyë");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byVowel(vc: String) = values().find { it.vc eq vc }
    }
}

enum class Illocution(val short: String) : Precision {
    CONFIRMATIVE("CNF"),
    INFERENTIAL("INF"),
    INTUITIVE("ITU"),
    REVELATORY("REV"),
    HEARSAY("HSY"),
    UNSPECIFICED("USP"),
    DIRECTIVE("DIR"),
    INTERROGATIVE("IRG"),
    DECLARATIVE("DEC");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

enum class Sanction(val short: String) : Precision {
    PROPOSITIONAL("PPS"),
    EPISTEMIC("EPI"),
    ALLEGATIVE("ALG"),
    IMPUTATIVE("IPU"),
    REFUTATIVE("RFU"),
    REBUTTATIVE("REB"),
    CONJECTURAL("CJT"),
    EXPATIATIVE("EXV"),
    AXIOMATIC("AXM"),
    NULL("null");

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
    DEJECTIVE("DEJ", "zzg");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byGroup(cb: String) = values().find { it.cb eq cb }
    }
}

enum class Register(val short: String, val v: String) : Precision {
    DISCURSIVE("DSV", "a"),
    PARENTHETICAL("PNT", "e"),
    COGITANT("COG", "o"),
    EXAMPLIFICATIVE("EXM", "ö"),
    SPECIFICATIVE("SPF", "i"),
    MATHEMATICAL("MTH", "u"),
    CARRIER_END("CAR", "ë");

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }

    companion object {
        fun byVowel(v: String) = values().find { it.v eq v }
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

fun parseCd(c: String) : List<Precision> {
    val i = CD_CONSONANTS.indexOf(c.defaultForm())
    return listOf(Designation.values()[i % 2], Incorporation.values()[i/2 % 2], Version.values()[i / 8])
}

fun parseVn(v: String): List<Precision>? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1)
        return null
    return listOf(Context.values()[i / 9], Valence.values()[i % 9])
}

fun parseVk(s: String) : List<Precision>? {
    val i = VOWEL_FORM.indexOfFirst { it eq s }
    return if (i == -1) {
        null
    } else {
        listOf(Illocution.values()[i % 9], Sanction.values()[i / 9])
    }
}

fun parseSlotEleven(s: String, precision: Int, ignoreDefault: Boolean = false) : String? {
    val phase = Phase.byVowel(s)
    if (phase != null) {
        return phase.toString(precision, ignoreDefault)
    }
    val ben = Effect.BENEFICIAL.toString(precision)
    val det = Effect.DETRIMENTAL.toString(precision)
    val unk = Effect.UNKNOWN.toString(precision)
    when (s.defaultForm()) {
        "wai" -> return "1/$ben"
        "wau" -> return "2/$ben"
        "wei" -> return "3/$ben"
        "weu" -> return "a/$ben"
        "wëi" -> return unk
        "woi" -> return "a/$det"
        "wou" -> return "3/$det"
        "wiu" -> return "2/$det"
        "wui" -> return "1/$det"
    }
    val i = VOWEL_FORM.indexOfFirst { it eq s.substring(1) }
    if (i != -1 && (i/9 == 2 || i/9 == 3)) {
        val lev = Level.values()[i % 9].toString(precision)
        val r = when {
            i/9 == 2 && precision == 0 -> "r"
            i/9 == 2 && precision > 0 -> "(relative)"
            i/9 == 3 && precision == 0 -> "a"
            else -> "(absolute)"
        }
        return "$lev$r"
    }
    return Aspect.byT2(s)?.toString(precision)
}

fun parseVv(s: String) : List<Precision>? = when {
    "a" eq s  -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.UNFRAMED)
    "ai" eq s -> listOf(Designation.INFORMAL, Version.PROCESSUAL, Relation.FRAMED)
    "e" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.UNFRAMED)
    "ei" eq s -> listOf(Designation.INFORMAL, Version.COMPLETIVE, Relation.FRAMED)
    "i/u" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.UNFRAMED)
    "ui/iu" eq s -> listOf(Designation.FORMAL, Version.PROCESSUAL, Relation.FRAMED)
    "o" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.UNFRAMED)
    "oi" eq s -> listOf(Designation.FORMAL, Version.COMPLETIVE, Relation.FRAMED)
    else -> null
}

fun parseVr(s: String): List<Precision>? = when {
    "a" eq s -> listOf(Specification.BASIC, Stem.STEM_ONE, Function.STATIVE)
    "ai" eq s -> listOf(Specification.BASIC, Stem.STEM_ONE, Function.DYNAMIC)
    "ä" eq s -> listOf(Specification.BASIC, Stem.STEM_TWO, Function.STATIVE)
    "au" eq s -> listOf(Specification.BASIC, Stem.STEM_TWO, Function.DYNAMIC)
    "ia/öa" eq s -> listOf(Specification.BASIC, Stem.STEM_THREE, Function.STATIVE)
    "ua/aö" eq s -> listOf(Specification.BASIC, Stem.STEM_THREE, Function.DYNAMIC)
    "ao" eq s -> listOf(Specification.BASIC, Stem.STEM_ZERO, Function.STATIVE)
    "ae" eq s -> listOf(Specification.BASIC, Stem.STEM_ZERO, Function.DYNAMIC)
    "e" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ONE, Function.STATIVE)
    "ei" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ONE, Function.DYNAMIC)
    "ü" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_TWO, Function.STATIVE)
    "eu" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_TWO, Function.DYNAMIC)
    "ie/öe" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_THREE, Function.STATIVE)
    "ue/eö" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_THREE, Function.DYNAMIC)
    "ea" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ZERO, Function.STATIVE)
    "eo" eq s -> listOf(Specification.CONTENTIAL, Stem.STEM_ZERO, Function.DYNAMIC)
    "o" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ONE, Function.STATIVE)
    "oi" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ONE, Function.DYNAMIC)
    "ö" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_TWO, Function.STATIVE)
    "ou" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_TWO, Function.DYNAMIC)
    "io/uö" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_THREE, Function.STATIVE)
    "uo/iö" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_THREE, Function.DYNAMIC)
    "oa" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ZERO, Function.STATIVE)
    "oe" eq s -> listOf(Specification.CONSTITUTIVE, Stem.STEM_ZERO, Function.DYNAMIC)
    "u" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ONE, Function.STATIVE)
    "ui" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ONE, Function.DYNAMIC)
    "i" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_TWO, Function.STATIVE)
    "iu" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_TWO, Function.DYNAMIC)
    "ië/oë" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_THREE, Function.STATIVE)
    "uë/eë" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_THREE, Function.DYNAMIC)
    "ëi" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ZERO, Function.STATIVE)
    "ëu" eq s -> listOf(Specification.OBJECTIVE, Stem.STEM_ZERO, Function.DYNAMIC)
    else -> null
}


fun parsePersonalReference(s: String): List<Precision>? = when (s.defaultForm()) {
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
    "nç" -> listOf(Referent.INANIMATE_IMPERSONAL, Effect.NEUTRAL)
    "mç" -> listOf(Referent.INANIMATE_IMPERSONAL, Effect.BENEFICIAL)
    "ňç" -> listOf(Referent.INANIMATE_IMPERSONAL, Effect.DETRIMENTAL)
    "lç" -> listOf(Referent.NOMIC_REFERENT, Effect.NEUTRAL)
    "rç" -> listOf(Referent.NOMIC_REFERENT, Effect.BENEFICIAL)
    "řç" -> listOf(Referent.NOMIC_REFERENT, Effect.DETRIMENTAL)
    "rr" -> listOf(Referent.ABSTRACT_REFERENT, Effect.NEUTRAL)
    "č" -> listOf(Referent.ABSTRACT_REFERENT, Effect.BENEFICIAL)
    "j" -> listOf(Referent.ABSTRACT_REFERENT, Effect.DETRIMENTAL)
    else -> null
}

fun parseCa(s: String) : List<Precision>? {
    val elements = arrayListOf<Precision>()
    var original = s.defaultForm()
    if (original.isEmpty() || original.startsWith("h"))
        return null
    when (original) {
        "l" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.MONADIC, Essence.NORMAL)
        "d" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.POLYADIC, Essence.NORMAL)
        "lţ" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.POLYADIC, Essence.REPRESENTATIVE)
        "rţ" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.NOMIC, Essence.REPRESENTATIVE)
        "lz" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.ABSTRACT, Essence.NORMAL)
        "lž" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.ABSTRACT, Essence.REPRESENTATIVE)
        "ļt" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.PROXIMAL, Perspective.MONADIC, Essence.NORMAL)
        "ļk" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.INCIPIENT, Perspective.MONADIC, Essence.NORMAL)
        "ļp" -> return listOf(Uniplex.SPECIFIC, Affiliation.CONSOLIDATIVE, Extension.ATTENUATIVE, Perspective.MONADIC, Essence.NORMAL)
        "ls" -> return listOf(Similarity.MULTIPLEX_FUZZY, Connectedness.SEPARATE, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.MONADIC, Essence.NORMAL)
        "lš" -> return listOf(Similarity.MULTIPLEX_FUZZY, Connectedness.CONNECTED, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.MONADIC, Essence.NORMAL)
        "lf" -> return listOf(Similarity.MULTIPLEX_FUZZY, Connectedness.FUSED, Affiliation.CONSOLIDATIVE, Extension.DELIMITIVE, Perspective.MONADIC, Essence.NORMAL)
    }
    original = original.replace("nn", "ňy")
        .replace("cc", "cy")
        .replace("čč", "čy")
        .replace("nž", "ňg")
        .replace("mv", "nb")
        .replace("nz", "ňk")
        .replace("mz", "np")
        .replace("mž", "nf")
        .replace("rš(?=.)".toRegex(), "šf")
        .replace("rs(?=.)".toRegex(), "sf")
        .replace("j", "šs")
        .replace("ẓ", "sš")
        .replace("v", "tf")
        .replace("č", "tš")
        .replace("c", "ts")
        .replace("bb|pţ".toRegex(), "pb")
        .replace("gg|kţ".toRegex(), "kg")
        .replace("ḑ", "ţf")
        .replace("ž", "ţš")
        .replace("z", "ţs")
    val a = when {
        original.endsWith("ř") -> {
            elements.add(Perspective.MONADIC)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        original.endsWith("ll") || original.endsWith("hw") -> {
            if (original.length == 2) // All such checks are present to ensure that the standalone forms are properly used
                return null
            elements.add(Perspective.POLYADIC)
            elements.add(Essence.REPRESENTATIVE)
            2
        }
        original.endsWith("ļ") -> {
            if (original.length == 1)
                return null
            elements.add(Perspective.POLYADIC)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        original.endsWith("l") -> {
            if (original.length == 1)
                return null
            elements.add(Perspective.POLYADIC)
            elements.add(Essence.NORMAL)
            1
        }
        original.endsWith("h") || original.endsWith("v") -> {
            if (original.length == 1)
                return null
            elements.add(Perspective.NOMIC)
            elements.add(Essence.REPRESENTATIVE)
            1
        }
        original.endsWith("rr") -> {
            if (original.length == 2)
                return null
            elements.add(Perspective.NOMIC)
            elements.add(Essence.REPRESENTATIVE)
            2
        }
        original.endsWith("r") -> {
            elements.add(Perspective.NOMIC)
            elements.add(Essence.NORMAL)
            1
        }
        original.endsWith("w") -> {
            if (original.length == 1)
                return null
            elements.add(Perspective.ABSTRACT)
            elements.add(Essence.NORMAL)
            1
        }
        original.endsWith("y") -> {
            if (original.length == 1)
                return null
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
        "ļt" -> {
            elements.add(0, Extension.PROXIMAL)
            return elements
        }
        "ļk" -> {
            elements.add(0, Extension.INCIPIENT)
            return elements
        }
        "ļp" -> {
            elements.add(0, Extension.ATTENUATIVE)
            return elements
        }
    }
    val b = when (original.lastOrNull()) { // Dirty hack exploiting the fact that in Kotlin, 'void' functions return a singleton object called Unit
        't' -> if (original.length == 1) {
            elements.add(0, Extension.DELIMITIVE)
            null
        } else {
            elements.add(0, Extension.PROXIMAL)
        }
        'k' -> if (original.length == 1) {
            elements.add(0, Extension.DELIMITIVE)
            null
        } else {
            elements.add(0, Extension.INCIPIENT)
        }
        'p' -> if (original.length == 1) {
            elements.add(0, Extension.DELIMITIVE)
            null
        } else {
            elements.add(0, Extension.ATTENUATIVE)
        }
        'g' -> elements.add(0, Extension.GRADUATIVE)
        'b' -> elements.add(0, Extension.DEPLETIVE)
        else -> {
            elements.add(0, Extension.DELIMITIVE)
            null
        }
    }
    if (b == Unit)
        original = original.dropLast(1)
    when (original) {
        "ls" -> {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
            return elements
        }
        "lš" -> {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
            return elements
        }
        "lf" -> {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
            return elements
        }
    }
    val c = when (original.lastOrNull()) {
        's' -> elements.add(0, Affiliation.ASSOCIATIVE)
        'f' -> elements.add(0, Affiliation.VARIATIVE)
        'š' -> elements.add(0, Affiliation.COALESCENT)
        else -> {
            elements.add(0, Affiliation.CONSOLIDATIVE)
            null
        }
    }
    if (c == Unit)
        original = original.dropLast(1)
    when (original) {
        "ţ" -> elements.add(0, Uniplex.POTENTIAL)
        "lt" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.DUPLEX_SIMILAR)
        }
        "lk" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.DUPLEX_SIMILAR)
        }
        "lp" -> {
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.DUPLEX_SIMILAR)
        }
        "rt" -> {
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.DUPLEX_DISSIMILAR)
        }
        "rk" -> {
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.DUPLEX_DISSIMILAR)
        }
        "rp" -> {
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
        "s" -> {
            if (Affiliation.CONSOLIDATIVE in elements)
                return null
            elements.add(0, Connectedness.SEPARATE)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
        }
        "š" -> {
            if (Affiliation.CONSOLIDATIVE in elements)
                return null
            elements.add(0, Connectedness.CONNECTED)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
        }
        "f" -> {
            if (Affiliation.CONSOLIDATIVE in elements)
                return null
            elements.add(0, Connectedness.FUSED)
            elements.add(0, Similarity.MULTIPLEX_FUZZY)
        }
        "" -> elements.add(0, Uniplex.SPECIFIC)
        else -> return null
    }
    return elements
}