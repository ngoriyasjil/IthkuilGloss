package ithkuil.iv.gloss

enum class WordType {
    FORMATIVE,
    MODULAR_ADJUNCT,
    AFFIXUAL_ADJUNCT,
    AFFIXUAL_SCOPING_ADJUNCT,
    REFERENTIAL,
    COMBINATION_REFERENTIAL,
    REGISTER_ADJUNCT,
    BIAS_ADJUNCT,
    MOOD_CASESCOPE_ADJUNCT;
}

enum class RootMode {
    ROOT,
    AFFIX,
    REFERENCE;
}

enum class Shortcut {
    Y_SHORTCUT,
    W_SHORTCUT;
}

enum class Concatenation(override val short: String) : NoDefault {
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


@Suppress("unused")
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
            return values().find { it.short == s  }
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

@Suppress("unused")
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

@Suppress("unused")
enum class Phase(override val short: String) : NoDefault {
    PUNCTUAL("PCT"),
    ITERATIVE("ITR"),
    REPETITIVE("REP"),
    INTERMITTENT("ITM"),
    RECURRENT("RCT"),
    FREQUENTATIVE("FRE"),
    FRAGMENTATIVE("FRG"),
    VACILLATIVE("VAC"),
    FLUCTUATIVE("FLC");

    companion object {
        fun byForm(form: Int) = values()[form-1]
    }

}


class EffectAndPerson(private val person: String?, private val effect: Effect) : Glossable {

    override fun toString(o: GlossOptions): String {
        return if (person != null) {
            "$person:${effect.toString(o.showDefaults())}"
        } else effect.toString(o.showDefaults())
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

@Suppress("unused")
enum class Level(override val short: String) : NoDefault {
    MINIMAL("MIN"),
    SUBEQUATIVE("SBE"),
    INFERIOR("IFR"),
    DEFICIENT("DFT"),
    EQUATIVE("EQU"),
    SURPASSIVE("SUR"),
    SUPERLATIVE("SPL"),
    SUPEREQUATIVE("SPQ"),
    MAXIMAL("MAX");

    companion object {
        fun byForm(form: Int) = values()[form-1]
    }
}

enum class LevelRelativity(override val short: String) : Category {
    RELATIVE("r"),
    ABSOLUTE("a");
}

class LevelAndRelativity(
    private val level: Level,
    private val relativity: LevelRelativity
) : Glossable {

    override fun toString(o: GlossOptions): String {
        return level.toString(o) +
            (if (!o.verbose) "" else CATEGORY_SEPARATOR) +
            relativity.toString(o)
    }
}

@Suppress("unused")
enum class Aspect(override val short: String, val vn: String) : NoDefault {
    RETROSPECTIVE("RTR", "a"),
    PROSPECTIVE("PRS", "ä"),
    HABITUAL("HAB", "e"),
    PROGRESSIVE("PRG", "ï"),
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
        fun byVowel(vn: String) = values().find { vn isSameVowelAs it.vn }
    }
}

enum class Mood(override val short: String) : Category {
    FACTUAL("FAC"),
    SUBJUNCTIVE("SUB"),
    ASSUMPTIVE("ASM"),
    SPECULATIVE("SPC"),
    COUNTERFACTIVE("COU"),
    HYPOTHETICAL("HYP");
}

enum class CaseScope(override val short: String) : Category {
    NATURAL("CCN"),
    ANTECEDENT("CCA"),
    SUBALTERN("CCS"),
    QUALIFIER("CCQ"),
    PRECEDENT("CCP"),
    SUCCESSIVE("CCV");
}

@Suppress("unused")
enum class Case(override val short: String, val vc: String) : Category {
    //  Transrelative
    THEMATIC("THM", "a"),
    INSTRUMENTAL("INS", "ä"),
    ABSOLUTIVE("ABS", "e"),
    STIMULATIVE("STM", "ï"),
    AFFECTIVE("AFF", "i"),
    EFFECTUATIVE("EFF", "ö"),
    ERGATIVE("ERG", "o"),
    DATIVE("DAT", "ü"),
    INDUCIVE("IND", "u"),

    //  Appositive
    POSSESSIVE("POS", "ai"),
    PROPRIETIVE("PRP", "au"),
    GENITIVE("GEN", "ei"),
    ATTRIBUTIVE("ATT", "eu"),
    PRODUCTIVE("PDC", "ëi"),
    INTERPRETIVE("ITP", "ou"),
    ORIGINATIVE("OGN", "oi"),
    INTERDEPENDENT("IDP", "iu"),
    PARTITIVE("PAR", "ui"),

    //  Associative
    APPLICATIVE("APL", "ia/oä"),
    PURPOSIVE("PUR", "iä/uä"),
    TRANSMISSIVE("TRA", "ie/oë"),
    DEFERENTIAL("DFR", "ië/uë"),
    CONTRASTIVE("CRS", "ëu"),
    TRANSPOSITIVE("TSP", "uö/iö"),
    COMMUTATIVE("CMM", "uo/io"),
    COMPARATIVE("CMP", "ue/eö"),
    CONSIDERATIVE("CSD", "ua/aö"),

    // Adverbial
    FUNCTIVE("FUN", "ao"),
    TRANSFORMATIVE("TFM", "ae"),
    CLASSIFICATIVE("CLA", "ea"),
    RESULTATIVE("RSL", "eo"),
    CONSUMPTIVE("CSM", "eë"),
    CONCESSIVE("CON", "öe"),
    AVERSIVE("AVS", "oe"),
    CONVERSIVE("CVS", "öa"),
    SITUATIVE("SIT", "oa"),

    // Relational
    PERTINENTIAL("PRN", "a'a"),
    DESCRIPTIVE("DSP", "ä'ä"),
    CORRELATIVE("COR", "e'e"),
    COMPOSITIVE("CPS", "ï'ï"),
    COMITATIVE("COM", "i'i"),
    UTILITATIVE("UTL", "ö'ö"),
    PREDICATIVE("PRD", "o'o"),
    RELATIVE("RLT", "u'u"),

    // Affinitive
    ACTIVATIVE("ACT", "a'i"),
    ASSIMILATIVE("ASI", "a'u"),
    ESSIVE("ESS", "e'i"),
    TERMINATIVE("TRM", "e'u"),
    SELECTIVE("SEL", "ë'i"),
    CONFORMATIVE("CFM", "o'u"),
    DEPENDENT("DEP", "o'i"),
    VOCATIVE("VOC", "u'i"),

    //  Spatio-Temporal I
    LOCATIVE("LOC", "i'a"),
    ATTENDANT("ATD", "i'ä"),
    ALLATIVE("ALL", "i'e"),
    ABLATIVE("ABL", "i'ë"),
    ORIENTATIVE("ORI", "ë'u"),
    INTERRELATIVE("IRL", "u'ö"),
    INTRATIVE("INV", "u'o"),
    NAVIGATIVE("NAV", "u'a"),

    // Spatio-Temporal II
    CONCURSIVE("CNR", "a'o"),
    ASSESSIVE("ASS", "a'e"),
    PERIODIC("PER", "e'a"),
    PROLAPSIVE("PRO", "e'o"),
    PRECURSIVE("PCV", "e'ë"),
    POSTCURSIVE("PCR", "ö'e"),
    ELAPSIVE("ELP", "o'e"),
    PROLIMITIVE("PLM", "o'a");

    companion object {
        fun byVowel(vc: String) = values().find { vc isSameVowelAs it.vc }
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

@Suppress("unused")
enum class Bias(override val short: String, val cb: String, private val representative: String) : NoDefault {
    DOLOROUS("DOL", "řřx", "Ow! Ouch!"),
    SKEPTICAL("SKP", "rnž", "Yeah, right!"),
    IMPATIENT("IPT", "žžv", "C'mon!"),
    REVELATIVE("RVL", "mmļ", "A-ha!"),
    TREPIDATIVE("TRP", "llč", "Oh, no!"),
    REPULSIVE("RPU", "šštļ", "Ew! Gross!"),
    DESPERATIVE("DES", "mřř", "I'm sorry to have to tell you..."),
    DISAPPROBATIVE("DPB", "ffx", "I don't like that..."),
    PROSAIC("PSC", "žžt", "Meh."),
    COMEDIC("CMD", "pļļ", "Funny!"),
    PROPOSITIVE("PPV", "sl", "Consider:"),
    SUGGESTIVE("SGS", "ltç", "How about..."),
    DIFFIDENT("DFD", "cč", "It's nothing, just..."),
    REFLECTIVE("RFL", "llm", "Look at it this way..."),
    EUPHEMISTIC("EUP", "vvt", "Let me put it this way..."),
    CORRECTIVE("CRR", "ňţ", "What I meant to say is..."),
    CONTEMPTIVE("CTP", "kšš", "What nonsense!"),
    EXASPERATIVE("EXA", "kçç", "Don't you get it?"),
    INDIGNATIVE("IDG", "pšš", "How dare...!?"),
    DISMISSIVE("DIS", "kff", "So what!"),
    DERISIVE("DRS", "pfc", "How foolish!"),
    PESSIMISTIC("PES", "ksp", "Pfft!"),
    DUBITATIVE("DUB", "mmf", "I doubt it"),
    INVIDIOUS("IVD", "řřn", "How unfair!"),
    DISCONCERTIVE("DCC", "gzj", "I don't feel confortable about this..."),
    STUPEFACTIVE("STU", "ļļč", "What the...?"),
    FASCINATIVE("FSC", "žžj", "Cool! Wow!"),
    INFATUATIVE("IFT", "vvr", "Praise be to...!"),
    EUPHORIC("EUH", "gzz", "What bliss!"),
    DELECTATIVE("DLC", "ẓmm", "Whee!"),
    ATTENTIVE("ATE", "ňj", "Who would have thought?"),
    APPROBATIVE("APB", "řs", "OK"),
    IRONIC("IRO", "mmž", "Just great!"),
    PRESUMPTIVE("PSM", "nnţ", "It can only mean one thing..."),
    GRATIFICATIVE("GRT", "mmh", "Ahhhh! [physical pleasure]"),
    SATIATIVE("SAT", "ļţ", "How satisfying!"),
    PERPLEXIVE("PPX", "llh", "Huh?"),
    CONTEMPLATIVE("CTV", "gvv", "Hmmmm..."),
    PROPITIOUS("PPT", "mll", "It's a wonder that..."),
    SOLICITATIVE("SOL", "ňňs", "Please"),
    REACTIVE("RAC", "kll", "My goodness!"),
    COINCIDENTAL("COI", "ššč", "What a coincidence!"),
    FORTUITOUS("FOR", "lzp", "All is well that ends well"),
    ANNUNCIATIVE("ANN", "drr", "Wait till you hear this!"),
    OPTIMAL("OPT", "ççk", "So!/Totally!"),
    CONTENSIVE("CNV", "rrj", "I told you so!"),
    MANDATORY("MND", "msk", "Take it or leave it"),
    EXIGENT("EXG", "rrs", "It's now or never!"),
    INSIPID("ISP", "lçp", "How boring!"),
    ADMISSIVE("ADM", "lļ", "Mm-hm"),
    APPREHENSIVE("APH", "vvz", "I'm worried..."),
    IMPLICATIVE("IPL", "vll", "Of course,..."),
    ACCIDENTAL("ACC", "lf", "As luck would would have it..."),
    ANTICIPATIVE("ANP", "lst", ""),
    ARCHETYPAL("ACH", "mçt", "Such a...!"),
    VEXATIVE("VEX", "ksk", "How annoying!"),
    CORRUPTIVE("CRP", "gžž", "What corruption!"),
    DEJECTIVE("DEJ", "žžg", "[dejected sigh]"),
    // Up-to-date in Roots; out-of-date in Morpho-phonology
    RENUNCIATIVE("RNC", "mst", "So much for...!"),
    ARBITRARY("ARB", "xtļ", "Yeah, whatever..."),
    EXPERIENTIAL("EXP", "pss", "Well, now!");

    override fun toString(o: GlossOptions): String = when {
        o.concise -> short
        o.verbose -> "(${name.toLowerCase()}: “$representative“)"
        else -> "“${representative}“"
    }

    companion object {
        fun byGroup(cb: String) = values().find { it.cb == cb }
    }
}

class RegisterAdjunct(private val register: Register, private val final: Boolean) : Glossable {
    override fun toString(o: GlossOptions): String {
        return when (final) {
            false -> register.toString(o)
            true -> "${register.toString(o)}_END"
        }
    }

}

@Suppress("unused")
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

            val matchInitial = values().find { it.initial == v }
            if (matchInitial != null) return matchInitial to false

            val matchFinal = values().find { it.final == v }
            if (matchFinal != null) return matchFinal to true

            return null
        }
    }
}

@Suppress("unused")
enum class Referent(override val short: String, private vararg val forms: String) : NoDefault {
    MONADIC_SPEAKER("1m", "l", "r", "ř"),
    MONADIC_ADDRESSEE("2m", "s", "š", "ž"),
    POLYADIC_ADDRESSEE("2p", "n", "t", "d"),
    MONADIC_ANIMATE_THIRD_PARTY("ma", "m", "p", "b"),
    POLYADIC_ANIMATE_THIRD_PARTY("pa", "ň", "k", "g"),
    MONADIC_INANIMATE_THIRD_PARTY("mi", "z", "ţ", "ḑ"),
    POLYADIC_INANIMATE_THIRD_PARTY("pi", "ẓ", "ļ", "f", "v"),
    MIXED_THIRD_PARTY("Mx", "c", "č", "j"),
    OBVIATIVE("Obv", "th", "ph", "kh"),
    PROVISIONAL("PVS", "ll", "rr", "řř"),
    CARRIER("[CAR]", "hl"),
    QUOTATIVE("[QUO]", "hm"),
    NAMING("[NAM]", "hn"),
    PHRASAL("[PHR]", "hň");

    companion object {
        fun byForm(c: String): Referent? = values().find { c in it.forms }
    }
}