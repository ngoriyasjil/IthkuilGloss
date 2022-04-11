package ithkuil.iv.gloss

enum class WordType {
    FORMATIVE,
    MODULAR_ADJUNCT,
    AFFIXUAL_ADJUNCT,
    MULTIPLE_AFFIX_ADJUNCT,
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
    Y,
    W;
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
    ZERO("S0"),
    ONE("S1"),
    TWO("S2"),
    THREE("S3");

    override fun gloss(o: GlossOptions) = when {
        o.verbose -> "stem_${name.lowercase()}"
        else -> short
    }
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

interface CaCategory : Category // Should ideally be sealed, but that feature isn't yet implemented

@Suppress("unused")
enum class Configuration(override val short: String) : CaCategory {
    UNIPLEX("UPX"),
    DUPLEX("DPX"),
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
        fun byAbbreviation(s: String): Configuration? {
            return values().find { it.short == s }
        }
    }

}

enum class Affiliation(override val short: String) : CaCategory {
    CONSOLIDATIVE("CSL"),
    ASSOCIATIVE("ASO"),
    VARIATIVE("VAR"),
    COALESCENT("COA");
}

enum class Extension(override val short: String) : CaCategory {
    DELIMITIVE("DEL"),
    PROXIMAL("PRX"),
    INCEPTIVE("ICP"),
    ATTENUATIVE("ATV"),
    GRADUATIVE("GRA"),
    DEPLETIVE("DPL");
}

enum class Perspective(override val short: String) : CaCategory {
    MONADIC("M"),
    AGGLOMERATIVE("G"),
    NOMIC("N"),
    ABSTRACT("A");
}

enum class Essence(override val short: String) : CaCategory {
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
        fun byForm(form: Int) = values()[form - 1]
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
        fun byForm(form: Int) = values()[form - 1]
    }

}


class EffectAndPerson(private val person: String?, private val effect: Effect) : Glossable {

    override fun gloss(o: GlossOptions): String {
        return if (person != null) {
            "$person:${effect.gloss(o.showDefaults())}"
        } else effect.gloss(o.showDefaults())
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
        fun byForm(form: Int) = values()[form - 1]
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

    constructor(form: Int, absoluteLevel: Boolean) : this(
        Level.byForm(form),
        if (absoluteLevel) LevelRelativity.ABSOLUTE else LevelRelativity.RELATIVE
    )

    override fun gloss(o: GlossOptions): String {
        return level.gloss(o) +
            (if (!o.verbose) "" else CATEGORY_SEPARATOR) +
            relativity.gloss(o)
    }
}

@Suppress("unused")
enum class Aspect(override val short: String, val series: Int, val form: Int) : NoDefault {
    RETROSPECTIVE("RTR", 1, 1),
    PROSPECTIVE("PRS", 1, 2),
    HABITUAL("HAB", 1, 3),
    PROGRESSIVE("PRG", 1, 4),
    IMMINENT("IMM", 1, 5),
    PRECESSIVE("PCS", 1, 6),
    REGULATIVE("REG", 1, 7),
    SUMMATIVE("SMM", 1, 8),
    ANTICIPATORY("ATP", 1, 9),

    RESUMPTIVE("RSM", 2, 1),
    CESSATIVE("CSS", 2, 2),
    PAUSAL("PAU", 2, 3),
    REGRESSIVE("RGR", 2, 4),
    PRECLUSIVE("PCL", 2, 5),
    CONTINUATIVE("CNT", 2, 6),
    INCESSATIVE("ICS", 2, 7),
    EXPERIENTIAL("EXP", 2, 8),
    INTERRUPTIVE("IRP", 2, 9),

    PREEMPTIVE("PMP", 3, 1),
    CLIMACTIC("CLM", 3, 2),
    DILATORY("DLT", 3, 3),
    TEMPORARY("TMP", 3, 4),
    EXPENDITIVE("XPD", 3, 5),
    LIMITATIVE("LIM", 3, 6),
    EXPEDITIVE("EPD", 3, 7),
    PROTRACTIVE("PTC", 3, 8),
    PREPARATORY("PPR", 3, 9),

    DISCLUSIVE("DCL", 4, 1),
    CONCLUSIVE("CCL", 4, 2),
    CULMINATIVE("CUL", 4, 3),
    INTERMEDIATIVE("IMD", 4, 4),
    TARDATIVE("TRD", 4, 5),
    TRANSITIONAL("TNS", 4, 6),
    INTERCOMMUTATIVE("ITC", 4, 7),
    MOTIVE("MTV", 4, 8),
    SEQUENTIAL("SQN", 4, 9);

    companion object {
        fun byVowel(vn: String) = values().find {
            val (series, form) = seriesAndForm(vn)
            it.series == series && it.form == form
        }
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
enum class Case(override val short: String, val series: Int, val form: Int, val glottal: Boolean = false) : Category {
    //  Transrelative
    THEMATIC("THM", 1, 1),
    INSTRUMENTAL("INS", 1, 2),
    ABSOLUTIVE("ABS", 1, 3),
    AFFECTIVE("AFF", 1, 4),
    STIMULATIVE("STM", 1, 5),
    EFFECTUATIVE("EFF", 1, 6),
    ERGATIVE("ERG", 1, 7),
    DATIVE("DAT", 1, 8),
    INDUCIVE("IND", 1, 9),

    //  Appositive
    POSSESSIVE("POS", 2, 1),
    PROPRIETIVE("PRP", 2, 2),
    GENITIVE("GEN", 2, 3),
    ATTRIBUTIVE("ATT", 2, 4),
    PRODUCTIVE("PDC", 2, 5),
    INTERPRETATIVE("ITP", 2, 6),
    ORIGINATIVE("OGN", 2, 7),
    INTERDEPENDENT("IDP", 2, 8),
    PARTITIVE("PAR", 2, 9),

    //  Associative
    APPLICATIVE("APL", 3, 1),
    PURPOSIVE("PUR", 3, 2),
    TRANSMISSIVE("TRA", 3, 3),
    DEFERENTIAL("DFR", 3, 4),
    CONTRASTIVE("CRS", 3, 5),
    TRANSPOSITIVE("TSP", 3, 6),
    COMMUTATIVE("CMM", 3, 7),
    COMPARATIVE("CMP", 3, 8),
    CONSIDERATIVE("CSD", 3, 9),

    // Adverbial
    FUNCTIVE("FUN", 4, 1),
    TRANSFORMATIVE("TFM", 4, 2),
    CLASSIFICATIVE("CLA", 4, 3),
    RESULTATIVE("RSL", 4, 4),
    CONSUMPTIVE("CSM", 4, 5),
    CONCESSIVE("CON", 4, 6),
    AVERSIVE("AVS", 4, 7),
    CONVERSIVE("CVS", 4, 8),
    SITUATIVE("SIT", 4, 9),

    // Relational
    PERTINENTIAL("PRN", 1, 1, true),
    DESCRIPTIVE("DSP", 1, 2, true),
    CORRELATIVE("COR", 1, 3, true),
    COMPOSITIVE("CPS", 1, 4, true),
    COMITATIVE("COM", 1, 5, true),
    UTILITATIVE("UTL", 1, 6, true),
    PREDICATIVE("PRD", 1, 7, true),
    RELATIVE("RLT", 1, 9, true),

    // Affinitive
    ACTIVATIVE("ACT", 2, 1, true),
    ASSIMILATIVE("ASI", 2, 2, true),
    ESSIVE("ESS", 2, 3, true),
    TERMINATIVE("TRM", 2, 4, true),
    SELECTIVE("SEL", 2, 5, true),
    CONFORMATIVE("CFM", 2, 6, true),
    DEPENDENT("DEP", 2, 7, true),
    VOCATIVE("VOC", 2, 9, true),

    //  Spatio-Temporal I
    LOCATIVE("LOC", 3, 1, true),
    ATTENDANT("ATD", 3, 2, true),
    ALLATIVE("ALL", 3, 3, true),
    ABLATIVE("ABL", 3, 4, true),
    ORIENTATIVE("ORI", 3, 5, true),
    INTERRELATIVE("IRL", 3, 6, true),
    INTRATIVE("INV", 3, 7, true),
    NAVIGATIVE("NAV", 3, 9, true),

    // Spatio-Temporal II
    CONCURSIVE("CNR", 4, 1, true),
    ASSESSIVE("ASS", 4, 2, true),
    PERIODIC("PER", 4, 3, true),
    PROLAPSIVE("PRO", 4, 4, true),
    PRECURSIVE("PCV", 4, 5, true),
    POSTCURSIVE("PCR", 4, 6, true),
    ELAPSIVE("ELP", 4, 7, true),
    PROLIMITIVE("PLM", 4, 9, true);

    companion object {
        fun byVowel(vc: String): Case? {
            val glottal = '\'' in vc
            val (series, form) = seriesAndForm(unglottalizeVowel(vc))
            return values().find {
                it.form == form
                    && it.series == series
                    && it.glottal == glottal
            }
        }
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
    REPORTIVE("RPR"),
    PURPORTIVE("PUP"),
    CONVENTIONAL("CVN"),
    INFERENTIAL("INF"),
    INTUITIVE("ITU"),
    IMAGINARY("IMA");
}

@Suppress("unused")
enum class Bias(override val short: String, val cb: String, private val representative: String) : NoDefault {
    ACCIDENTAL("ACC", "lf", "As luck would would have it..."),
    ADMISSIVE("ADM", "lļ", "Mm-hm"),
    ANNUNCIATIVE("ANN", "drr", "Wait till you hear this!"),
    ANTICIPATIVE("ANP", "lst", ""),
    APPREHENSIVE("APH", "vvz", "I'm worried..."),
    APPROBATIVE("APB", "řs", "OK"),
    ARBITRARY("ARB", "xtļ", "Yeah, whatever..."),
    ARCHETYPAL("ACH", "mçt", "Such a...!"),
    ATTENTIVE("ATE", "ňj", "Who would have thought?"),
    COINCIDENTAL("COI", "ššč", "What a coincidence!"),
    COMEDIC("CMD", "pļļ", "Funny!"),
    CONTEMPLATIVE("CTV", "gvv", "Hmmmm..."),
    CONTEMPTIVE("CTP", "kšš", "What nonsense!"),
    CONTENSIVE("CNV", "rrj", "I told you so!"),
    CORRECTIVE("CRR", "ňţ", "What I meant to say is..."),
    CORRUPTIVE("CRP", "gžž", "What corruption!"),
    DEJECTIVE("DEJ", "žžg", "[dejected sigh]"),
    DELECTATIVE("DLC", "ẓmm", "Whee!"),
    DERISIVE("DRS", "pfc", "How foolish!"),
    DESPERATIVE("DES", "mřř", "I'm sorry to have to tell you..."),
    DIFFIDENT("DFD", "cč", "It's nothing, just..."),
    DISAPPROBATIVE("DPB", "ffx", "I don't like that..."),
    DISCONCERTIVE("DCC", "gzj", "I don't feel confortable about this..."),
    DISMISSIVE("DIS", "kff", "So what!"),
    DOLOROUS("DOL", "řřx", "Ow! Ouch!"),
    DUBITATIVE("DUB", "mmf", "I doubt it"),
    EUPHEMISTIC("EUP", "vvt", "Let me put it this way..."),
    EUPHORIC("EUH", "gzz", "What bliss!"),
    EXASPERATIVE("EXA", "kçç", "Don't you get it?"),
    EXIGENT("EXG", "rrs", "It's now or never!"),
    EXPERIENTIAL("EXP", "pss", "Well, now!"),
    FASCINATIVE("FSC", "žžj", "Cool! Wow!"),
    FORTUITOUS("FOR", "lzp", "All is well that ends well"),
    GRATIFICATIVE("GRT", "mmh", "Ahhhh! [physical pleasure]"),
    IMPATIENT("IPT", "žžv", "C'mon!"),
    IMPLICATIVE("IPL", "vll", "Of course,..."),
    INDIGNATIVE("IDG", "pšš", "How dare...!?"),
    INFATUATIVE("IFT", "vvr", "Praise be to...!"),
    INSIPID("ISP", "lçp", "How boring!"),
    INVIDIOUS("IVD", "řřn", "How unfair!"),
    IRONIC("IRO", "mmž", "Just great!"),
    MANDATORY("MND", "msk", "Take it or leave it"),
    OPTIMAL("OPT", "ççk", "So!/Totally!"),
    PERPLEXIVE("PPX", "llh", "Huh?"),
    PESSIMISTIC("PES", "ksp", "Pfft!"),
    PRESUMPTIVE("PSM", "nnţ", "It can only mean one thing..."),
    PROPITIOUS("PPT", "mll", "It's a wonder that..."),
    PROPOSITIVE("PPV", "sl", "Consider:"),
    PROSAIC("PSC", "žžt", "Meh."),
    REACTIVE("RAC", "kll", "My goodness!"),
    REFLECTIVE("RFL", "llm", "Look at it this way..."),
    RENUNCIATIVE("RNC", "mst", "So much for...!"),
    REPULSIVE("RPU", "šštļ", "Ew! Gross!"),
    REVELATIVE("RVL", "mmļ", "A-ha!"),
    SATIATIVE("SAT", "ļţ", "How satisfying!"),
    SKEPTICAL("SKP", "rnž", "Yeah, right!"),
    SOLICITATIVE("SOL", "ňňs", "Please"),
    STUPEFACTIVE("STU", "ļļč", "What the...?"),
    SUGGESTIVE("SGS", "ltç", "How about..."),
    TREPIDATIVE("TRP", "llč", "Oh, no!"),
    VEXATIVE("VEX", "ksk", "How annoying!");

    override fun gloss(o: GlossOptions): String = when {
        o.concise -> short
        o.verbose -> "(${name.lowercase()}: “$representative“)"
        else -> "“${representative}“"
    }

    companion object {
        fun byCb(cb: String) = values().find { it.cb == cb }
    }
}

class RegisterAdjunct(private val register: Register, private val final: Boolean) : Glossable {
    override fun gloss(o: GlossOptions): String {
        return when (final) {
            false -> register.gloss(o)
            true -> "${register.gloss(o)}_END"
        }
    }

}

@Suppress("unused")
enum class Register(override val short: String, val initial: String, val final: String) : NoDefault {
    DISCURSIVE("DSV", "a", "ai"),
    PARENTHETICAL("PNT", "e", "ei"),
    SPECIFICATIVE("SPF", "i", "iu"),
    EXAMPLIFICATIVE("EXM", "o", "oi"),
    COGITANT("CGT", "ö", "öi"),
    MATHEMATICAL("MTH", "u", "ui"),
    CARRIER_END("CAR", "", "ü");

    companion object {
        fun adjunctByVowel(v: String): RegisterAdjunct? {

            val matchesInitial = values().find { it.initial == v }
            if (matchesInitial != null) return RegisterAdjunct(matchesInitial, false)

            val matchesFinal = values().find { it.final == v }
            if (matchesFinal != null) return RegisterAdjunct(matchesFinal, true)

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
    OBVIATIVE("Obv", "ll", "rr", "řř"),
    REDUPLICATIVE("Rdp", "th", "ph", "kh"),
    PROVISIONAL("PVS", "mm", "nn", "ňň"),
    CARRIER("[CAR]", "hl"),
    QUOTATIVE("[QUO]", "hm"),
    NAMING("[NAM]", "hn"),
    PHRASAL("[PHR]", "hň");

    companion object {
        fun byForm(c: String): Referent? = values().find { c in it.forms }
    }
}