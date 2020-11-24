package io.github.syst3ms.tnil

import java.lang.IllegalStateException

val flatVowelForm = VOWEL_FORM.flatMap { it.split("/") }
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

fun seriesAndForm(v: String) : Pair<Int, Int> {
    return when (val index = VOWEL_FORM.indexOfFirst { it eq v }) {
        -1 -> Pair(-1, -1)
        else -> Pair((index / 9) + 1, (index % 9) + 1)
    }
}

fun bySeriesAndForm(series: Int, form: Int) : String? = if (series in 1..8 && form in 1..9) VOWEL_FORM.getOrNull(9 * (series-1) + (form-1)) else null

fun unGlottalVowel(v: String) : Pair<String, Boolean>? {
    val (series, form) = seriesAndForm(v)
    return if (series >= 4) {
        (bySeriesAndForm(series - 4, form)?.to(true)) ?: return null
    } else {
        v to false
    }
}



fun parseCaseAffixVowel(v: String, secondHalf: Boolean) : Case? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1 || secondHalf && i % 9 == 7) // one of the unused values
        return null
    return if (secondHalf) {
        when {
            i % 9 == 8 -> Case.values()[36 + i - i/9 - 1] // We have to move back 1 for each 8th vowel form that's skipped
            else -> Case.values()[36 + i - i/9] // Likewise
        }
    } else {
        Case.values()[i]
    }
}

enum class Shortcut{
    Y_SHORTCUT,
    W_SHORTCUT;
}

fun parseCc(c: String) : Pair<Concatenation?, Shortcut?> {
    val concatenation = when (c) {
        "h", "hl", "hm" -> Concatenation.TYPE_ONE
        "hw", "hr", "hn" -> Concatenation.TYPE_TWO
        else -> null
    }

    val shortcut = when (c) {
        "w", "hl", "hr" -> Shortcut.W_SHORTCUT
        "y", "hm", "hn" -> Shortcut.Y_SHORTCUT
        else -> null
    }

    return Pair(concatenation, shortcut)
}

fun parseVv(v: String, shortcut: Shortcut?) : List<Precision>? {
    val (series, form) = seriesAndForm(v)

    val stem = when(form) {
        1, 2 -> Stem.STEM_ONE
        3, 5 -> Stem.STEM_TWO
        9, 8 -> Stem.STEM_THREE
        7, 6 -> Stem.STEM_ZERO
        else -> return null
    }
    val version = when(form) {
        1, 3, 9, 7 -> Version.PROCESSUAL
        2, 5, 8, 6 -> Version.COMPLETIVE
        else -> return null
    }

    var additional : List<Precision> = listOf()

    when (shortcut) {
        null -> {
            additional = when (series) {
                1 -> emptyList()
                2 -> listOf(Affix("ë", "r"))
                3 -> listOf(Affix("ë", "t"))
                4 -> listOf(Affix("i", "t"))
                else -> return null
            }
        }
        Shortcut.W_SHORTCUT -> {
            additional = when (series) {
                1 -> parseCa("l")!!
                2 -> parseCa("r")!!
                3 -> parseCa("v")!!
                4 -> parseCa("tļ")!!
                else -> return null
            }
        }
        Shortcut.Y_SHORTCUT -> {
            additional = when (series) {
                1 -> parseCa("s")!!
                2 -> parseCa("ř")!!
                3 -> parseCa("z")!!
                4 -> parseCa("sř")!!
                else -> return null
            }
        }
    }

    return listOf(stem, version) + additional

}

class Affix(val vx: String, val cs : String) : Precision { //Definitely not final

    override fun toString(precision: Int, ignoreDefault: Boolean): String
            = parseAffix(this.cs, this.vx, precision, ignoreDefault)

}


fun parseVnPatternOne(v: String, precision: Int, ignoreDefault: Boolean): String? {
    val i = VOWEL_FORM.indexOfFirst { it eq v }
    if (i == -1 || i in 36..71)
        return null
    return when {
        i < 9 -> Valence.values()[i % 9].toString(precision, ignoreDefault)
        i < 18 -> Phase.values()[i % 9].toString(precision, ignoreDefault)
        i < 27 -> effectString(precision, i % 9)
        else -> Level.values()[i % 9].toString(precision, false) + (if (i >= 72 && precision > 0) "(abs)" else if (i >= 72) "a" else "")
    }
}

fun effectString(precision: Int, effectIndex: Int): String? {
    val ben = Effect.BENEFICIAL.toString(precision)
    val det = Effect.DETRIMENTAL.toString(precision)
    val unk = Effect.UNKNOWN.toString(precision)
    return when (effectIndex) {
        0 -> "1:$ben"
        1 -> "2:$ben"
        2 -> "3:$ben"
        3 -> "SLF:$ben"
        4 -> unk
        5 -> "SLF:$det"
        6 -> "3:$det"
        7 -> "2:$det"
        8 -> "1:$det"
        else -> throw IllegalStateException()
    }
}

fun parseVk(s: String) : List<Precision>? {
    val (series, form) = seriesAndForm(s)

    val illocution = if (form == 5) Illocution.PERFORMATIVE else Illocution.ASSERTIVE
    val expectation = when (series) {
        1 -> Expectation.COGNITIVE
        2 -> Expectation.RESPONSIVE
        3 -> Expectation.EXECUTIVE
        else -> null
    }
    val validation = when(form) {
        1 -> Validation.OBSERVATIONAL
        2 -> Validation.RECOLLECTIVE
        3 -> Validation.REPORTIVE
        4 -> Validation.PURPORTIVE
        5 -> null
        6 -> Validation.IMAGINARY
        7 -> Validation.CONVENTIONAL
        8 -> Validation.INTUITIVE
        9 -> Validation.INFERENTIAL
        else -> null
    }
    val values = listOfNotNull(illocution, expectation, validation)

    return if (values.size > 1) values else null
}

fun parseSimpleVv(s: String): Pair<List<Precision>, Boolean>? {
    val (series, form) = seriesAndForm(s.replace("[wy]".toRegex(), "'"))
    val stem = when(form) {
        1, 2 -> Stem.STEM_ONE
        3, 5 -> Stem.STEM_TWO
        9, 8 -> Stem.STEM_THREE
        7, 6 -> Stem.STEM_ZERO
        else -> return null
    }
    val version = when(form) {
        1, 3, 9, 7 -> Version.PROCESSUAL
        2, 5, 8, 6 -> Version.COMPLETIVE
        else -> return null
    }
    val context = when(series) {
        1, 5 -> Context.EXISTENTIAL
        2, 6 -> Context.FUNCTIONAL
        3, 7 -> Context.REPRESENTATIONAL
        4, 8 -> Context.AMALGAMATIVE
        else -> return null
    }
    val negShortcut = when(series) {
        5, 6, 7, 8 -> true
        else -> false
    }

    return Pair(listOf(stem, version, context), negShortcut)

}

fun parseVr(v: String): List<Precision>? {
    val (series, form) = seriesAndForm(v)

    val specification = when (form) {
        1, 9 -> Specification.BASIC
        2, 8 -> Specification.CONTENTIAL
        3, 7 -> Specification.CONSTITUTIVE
        5, 6 -> Specification.OBJECTIVE
        else -> return null
    }
    val function = when (form) {
        1, 2, 3, 5 -> Function.STATIVE
        9, 8, 7, 6 -> Function.DYNAMIC
        else -> return null
    }

    val context = when(series) {
        1 -> Context.EXISTENTIAL
        2 -> Context.FUNCTIONAL
        3 -> Context.REPRESENTATIONAL
        4 -> Context.AMALGAMATIVE
        else -> return null
    }

    return listOf(function, specification, context)

}

fun parseVnCn(vn: String, cn: String, marksMood: Boolean): List<Precision>? {
    val pattern = when (cn) {
        "h", "hl", "hr", "hm", "hn", "hň" -> 1
        "w", "y", "hw", "hlw", "hly", "hnw", "hny" -> 2
        else -> return null
    }

    val (series, form) = seriesAndForm(vn)

    val vnValue: Precision = if (pattern == 1) {
        when (series) {
            1 -> Valence.byForm(form)
            2 -> Phase.byForm(form)
            3 -> EffectAndPerson.byForm(form)
            4 -> Level.byForm(form)
            else -> return null
        }
    } else {
        Aspect.byVowel(vn) ?: return null
    }

    val cnValue: Precision = if (marksMood) {
        when (cn) {
            "h", "w", "y" -> Mood.FACTUAL
            "hl", "hw" -> Mood.SUBJUNCTIVE
            "hr", "hlw" -> Mood.ASSUMPTIVE
            "hm", "hly" -> Mood.SPECULATIVE
            "hn", "hnw" -> Mood.COUNTERFACTIVE
            "hň", "hny" -> Mood.HYPOTHETICAL
            else -> return null
        }
    } else {
        when (cn) {
            "h", "w", "y" -> CaseScope.NATURAL
            "hl", "hw" -> CaseScope.ANTECEDENT
            "hr", "hlw" -> CaseScope.SUBALTERN
            "hm", "hly" -> CaseScope.QUALIFIER
            "hn", "hnw" -> CaseScope.PRECEDENT
            "hň", "hny" -> CaseScope.SUCCESSIVE
            else -> return null
        }
    }

    return listOf(vnValue, cnValue)

}

fun parseCbCy(s: String, marksMood: Boolean): Precision? {
    val c = s.removePrefix("'")
    val bias = Bias.byGroup(c)
    val cyValue = if (bias == null) {
        if (marksMood) {
            when (c) {
                "x" -> Mood.SUBJUNCTIVE
                "rs" -> Mood.ASSUMPTIVE
                "rš" -> Mood.SPECULATIVE
                "rz" -> Mood.COUNTERFACTIVE
                "rž" -> Mood.HYPOTHETICAL
                else -> return null
            }
        } else {
            when (c) {
                "x" -> CaseScope.ANTECEDENT
                "rs" -> CaseScope.SUBALTERN
                "rš" -> CaseScope.QUALIFIER
                "rz" -> CaseScope.PRECEDENT
                "rž" -> CaseScope.SUCCESSIVE
                else -> return null
            }
        }
    } else null

    return bias ?: cyValue

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
    "tļ" -> listOf(Referent.POLYADIC_INANIMATE_THIRD_PARTY, Effect.NEUTRAL)
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
    "çň", "ňç" -> if (final || r == "çň") listOf<Precision>(Referent.INANIMATE_IMPERSONAL, Effect.DETRIMENTAL) else null
    "çl", "lç" -> if (final || r == "çl") listOf<Precision>(Referent.NOMIC_REFERENT, Effect.NEUTRAL) else null
    "çr", "rç" -> if (final || r == "çr") listOf<Precision>(Referent.NOMIC_REFERENT, Effect.BENEFICIAL) else null
    "çř", "řç" -> if (final || r == "çř") listOf<Precision>(Referent.NOMIC_REFERENT, Effect.DETRIMENTAL) else null
    "rr" -> listOf(Referent.ABSTRACT_REFERENT, Effect.NEUTRAL)
    "č" -> listOf(Referent.ABSTRACT_REFERENT, Effect.BENEFICIAL)
    "j" -> listOf(Referent.ABSTRACT_REFERENT, Effect.DETRIMENTAL)
    else -> null
}


fun String.isGlottalCa(): Boolean = when {
    startsWith("'") -> true
    length == 2 && this[0] == this[1] && (this[0] in STOPS || this[0] in AFFRICATES)  -> true
    length > 2 && this[1] == this[2] && this[0] in setOf('p', 't', 'k') && this[1] in FRICATIVES -> true
    length > 2 && this[0] == this[1] && this[0] in setOf('r','l') union NASALS union FRICATIVES union AFFRICATES -> true
    else -> false
}



fun String.unGlottalCa(): String = when {
    startsWith("'") -> this.drop(1)
    length == 2 && this[0] == this[1] && (this[0] in STOPS || this[0] in AFFRICATES)  -> this.drop(1)
    length > 2 && this[1] == this[2] && this[0] in setOf('p', 't', 'k') && this[1] in FRICATIVES -> this[0] + this.drop(2)
    this in UNGLOTTAL_MAP.keys -> UNGLOTTAL_MAP[this] ?: this
    length > 2 && this[0] == this[1] && this[0] in setOf('r','l') union NASALS union FRICATIVES union AFFRICATES -> this.drop(1)
    else -> this
}


fun parseCa(s: String) : List<Precision>? {
    var original = s.defaultForm()
    if (original.isEmpty())
        return null

    var similarity = Similarity.UNIPLEX
    var separability: Connectedness? = null
    var extension = Extension.DELIMITIVE
    var affiliation = Affiliation.CONSOLIDATIVE
    var perspective = Perspective.MONADIC
    var essence = Essence.NORMAL

    var standaloneForm = true

    when (original) {
        "d" -> affiliation = Affiliation.ASSOCIATIVE
        "g" -> affiliation = Affiliation.COALESCENT
        "b" -> affiliation = Affiliation.VARIATIVE
        "l", "ř" -> Unit
        "r", "tļ" -> perspective = Perspective.POLYADIC
        "v", "lm" -> perspective = Perspective.NOMIC
        "z", "ln" -> perspective = Perspective.ABSTRACT
        else -> standaloneForm = false
    }

    if (standaloneForm) {
        if (original in setOf("ř","tļ", "lm", "ln")) {
            essence = Essence.REPRESENTATIVE
        }
        return listOf(similarity, extension, affiliation, perspective, essence)
    }

    CA_SUBSTITUTIONS.forEach{ (substitution, normal) -> original.replace(substitution, normal) }

    when {
        original[0] == 'l' -> {
            similarity = Similarity.MULTIPLEX_FUZZY
            original = original.drop(1)
        }
        original[0] in setOf('r', 'ř') -> {
            similarity = when (original.take(2)) {
                "rt", "rk", "rp" -> Similarity.DUPLEX_SIMILAR
                "rn", "rň", "rm" -> Similarity.DUPLEX_DISSIMILAR
                "řt", "řk", "řp" -> Similarity.DUPLEX_FUZZY
                else -> return null
            }
            original = original.drop(1)
        }
        else -> {
            similarity = when (original[0]) {
                't', 'k', 'p' -> Similarity.MULTIPLEX_SIMILAR
                'n', 'ň', 'm' -> Similarity.MULTIPLEX_DISSIMILAR
                else -> Similarity.UNIPLEX
            }
        }
    }

    separability = when (original[0]) {
        't', 'n' -> Connectedness.SEPARATE
        'k', 'ň' -> Connectedness.CONNECTED
        'p', 'm' -> Connectedness.FUSED
        else -> null
    }

    if (original.getOrNull(0) in setOf('s', 'š', 'f', 'ţ', 'ç')) {
        extension = when (original[0]) {
            's' -> Extension.PROXIMAL
            'š' -> Extension.INCIPIENT
            'f' -> Extension.ATTENUATIVE
            'ţ' -> Extension.GRADUATIVE
            'ç' -> Extension.DEPLETIVE
            else -> return null
        }
        original = original.drop(1)
    }

    if (original.getOrNull(0) in setOf('d', 'g', 'b', 't', 'k', 'p')) {
        affiliation = when (original[0]) {
            't', 'd' -> Affiliation.ASSOCIATIVE
            'k', 'g' -> Affiliation.COALESCENT
            'p', 'b' -> Affiliation.VARIATIVE
            else -> return null
        }
        original = original.drop(1)
    }

    if (original.isNotEmpty()) {
        perspective = when(original[0]) {
            'r', 'v', 'l' -> Perspective.POLYADIC
            'w', 'm', 'h' -> Perspective.NOMIC
            'y', 'n', 'ç' -> Perspective.ABSTRACT
            else -> return null
        }
        essence = when(original[0]) {
            'ř', 'l', 'm', 'h', 'n', 'ç' -> Essence.REPRESENTATIVE
            else -> Essence.NORMAL
        }
        original = original.drop(1)
    }
    return if (original.isNotEmpty()) null else {
        listOfNotNull(similarity, separability, extension, affiliation, perspective, essence)
    }
}

internal fun perspectiveIndexFromCa(ca: List<Precision>) = Perspective.values().indexOf(ca[ca.lastIndex - 1] as Perspective)

fun affixAdjunctScope(s: String?, ignoreDefault: Boolean, scopingAdjunctVowel: Boolean = false): String? {
    val scope = when (s?.defaultForm()) {
        null -> if (scopingAdjunctVowel) "{same}" else "{VDom}"
        "h", "a" -> "{VDom}"
        "'h", "u" -> "{VSub}"
        "'w", "e" -> "{VIIDom}"
        "'y", "i" -> "{VIISub}"
        "'hl", "o" -> "{formative}"
        "'hr", "ö" -> "{adjacent}"
        "ë" -> if (scopingAdjunctVowel) "{same}" else null
        else -> null
    }
    val default = (scope == "{VDom}" && !scopingAdjunctVowel) || (scope == "{same}" && scopingAdjunctVowel)

    return if (default && ignoreDefault) "" else scope
}

fun parseModularScope(vh: String, precision: Int, ignoreDefault: Boolean) : String? =
        when (vh.defaultForm()) {
            "a" -> if (!ignoreDefault) "{normal}" else ""
            "e" -> "{successive}"
            "i", "u" -> "{formative}"
            "o" -> "{adjacent}"
            else -> null
}

fun parseCarrierAdjuncts(typeC: String, caseV: String, precision: Int, ignoreDefault: Boolean) : String? {

    val type = when(typeC.defaultForm()) {
        "ç" ->  "[carrier]"
        "hl" -> "[quotative]"
        "hr" -> "[naming]"
        "hm" -> "[phrasal]"
        else -> null
    }

    val case = Case.byVowel(caseV.defaultForm())?.toString(precision, ignoreDefault)

    return if (type != null && case != null) {
        type + (if (!case.isEmpty()) "$SLOT_SEPARATOR$case" else "")
    } else null
}