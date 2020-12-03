package io.github.syst3ms.tnil

fun seriesAndForm(v: String) : Pair<Int, Int> {
    return when (val index = VOWEL_FORMS.indexOfFirst { it eq v }) {
        -1 -> Pair(-1, -1)
        else -> Pair((index / 9) + 1, (index % 9) + 1)
    }
}

fun bySeriesAndForm(series: Int, form: Int) : String? = if (series in 1..8 && form in 1..9) VOWEL_FORMS.getOrNull(9 * (series-1) + (form-1)) else null

fun unGlottalVowel(v: String) : Pair<String, Boolean>? {
    if (!v.isVowel()) return null

    if (v.length != 3) return v to false

    return if (v[1] == '\'') {
        if (v[0] == v[2]) {
            v[0].toString() to true
        } else "${v[0]}${v[2]}" to true
    } else v to false

}

fun glottalVowel(v: String) : Pair<String, Boolean>? {
    if (!v.isVowel()) return null

    return when (v.length) {
        1 -> "$v'$v" to true
        2 -> "${v[0]}'${v[1]}" to true
        else -> v to false
    }
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

fun parseVv(v: String, shortcut: Shortcut?) : Slot? {

    val (series, form) = seriesAndForm(v)

    if ((series == 1 && form == 4) || (series != 1 && form == 5)) return null

    val stem = when(form) {
        1, 2 -> Stem.STEM_ONE
        3, 5, 4 -> Stem.STEM_TWO
        9, 8 -> Stem.STEM_THREE
        7, 6 -> Stem.STEM_ZERO
        else -> return null
    }
    val version = when(form) {
        1, 3, 9, 7 -> Version.PROCESSUAL
        2, 5, 4, 8, 6 -> Version.COMPLETIVE
        else -> return null
    }

    val additional : Precision

    when (shortcut) {
        null -> {
            additional = when (series) {
                1 -> Slot()
                2 -> Affix("ë", "r", noType = true)
                3 -> Affix("ë", "t", noType = true)
                4 -> Affix("i", "t", noType = true)
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

    return Slot(stem, version, additional)

}

fun parseSpecialVv(vv: String, shortcut: Shortcut?): Slot? {
    val version = when (vv) {
        "ëi", "eë", "eä" -> Version.PROCESSUAL
        "ëu", "öë", "öä" -> Version.COMPLETIVE
        else -> return null
    }

    val function = when (vv) {
        "ëi", "ëu" -> Function.STATIVE
        "eë", "öë" -> Function.DYNAMIC
        else -> null
    }

    val ca = if (shortcut != null && vv in setOf("eä", "öä") ) {
        when (shortcut) {
            Shortcut.W_SHORTCUT -> parseCa("l")!!
            Shortcut.Y_SHORTCUT -> parseCa("s")!!
        }
    } else if (shortcut != null) {
        return null
    } else Slot()

    return Slot(version, function, ca)

}

fun parseVh(vh: String) : PrecisionString? = when (vh.defaultForm()) {
    "a" -> PrecisionString("{scope over formative}", "{form.}")
    "e" -> PrecisionString("{scope over case/mood}", "{mood}")
    "i", "u" -> PrecisionString("{scope over formative, but not adjacent adjuncts}", "{under adj.}")
    "o" -> PrecisionString("{scope over formative and adjacent adjuncts}", "{over adj.}")
    else -> null
}


fun parseVk(s: String) : Slot? {
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
    val values = Slot(illocution, expectation, validation)

    return if (values.size > 1) values else null
}


fun parseVr(v: String): Slot? {
    val (series, form) = seriesAndForm(v)

    if ((series == 1 && form == 4) || (series != 1 && form == 5)) return null

    val specification = when (form) {
        1, 9 -> Specification.BASIC
        2, 8 -> Specification.CONTENTIAL
        3, 7 -> Specification.CONSTITUTIVE
        5, 4, 6 -> Specification.OBJECTIVE
        else -> return null
    }
    val function = when (form) {
        1, 2, 3, 5, 4 -> Function.STATIVE
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

    return Slot(function, specification, context)

}

fun parseVnCn(vn: String, cn: String, marksMood: Boolean): Slot? {
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

    return Slot(vnValue, cnValue)

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

fun parsePersonalReference(s: String) : Slot? {
    val r = s.defaultForm()
    val referent = when (r) {
        "l", "r", "ř" -> Referent.MONADIC_SPEAKER
        "s", "š", "ž" -> Referent.MONADIC_ADDRESSEE
        "n", "t", "d" -> Referent.POLYADIC_ADDRESSEE
        "m", "p", "b" -> Referent.MONADIC_ANIMATE_THIRD_PARTY
        "ň", "k", "g" -> Referent.POLYADIC_ANIMATE_THIRD_PARTY
        "z", "ţ", "ḑ" -> Referent.MONADIC_INANIMATE_THIRD_PARTY
        "ẓ", "ļ", "f", "v" -> Referent.POLYADIC_INANIMATE_THIRD_PARTY
        "c", "č", "j" -> Referent.MIXED_THIRD_PARTY
        "th", "ph", "kh" -> Referent.OBVIATIVE
        "ll", "rr", "řř" -> Referent.PROVISIONAL
        "ç", "x" -> Perspective.NOMIC
        "w", "y" -> Perspective.ABSTRACT
        else -> return null
    }

    val effect = when (r) {
        "l", "s", "n", "m", "ň", "z", "ẓ", "ļ", "c", "th", "ll" -> Effect.NEUTRAL
        "r", "š", "t", "p", "k", "ţ", "f", "č", "ph", "rr" -> Effect.BENEFICIAL
        "ř", "ž", "d", "b", "g", "ḑ", "v", "j", "kh", "řř" -> Effect.DETRIMENTAL
        else -> null
    }

    return Slot(referent, effect)
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


fun parseCa(s: String) : Slot? {
    val original = s.defaultForm()
    if (original.isEmpty())
        return null

    var configuration = Configuration.UNIPLEX
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
        return Slot(configuration, extension, affiliation, perspective, essence)
    }

    val normal = CA_SUBSTITUTIONS.fold(original) { it, (substitution, normal) -> it.replace(substitution, normal) }
    var index = 0

    var conf: String

    when (normal[0]){
        'l' -> {
            conf = "MF"
            index++
        }
        'r', 'ř' -> {
            conf = when (normal.take(2)) {
                "rt", "rk", "rp" -> "DS"
                "rn", "rň", "rm" -> "DD"
                "řt", "řk", "řp" -> "DF"
                else -> return null
            }
            index++
        }
        else -> {
            conf = when (normal[0]) {
                't', 'k', 'p' -> "MS"
                'n', 'ň', 'm' -> "MD"
                else -> "UNI"
            }
        }
    }

    conf += when (normal[index]) {
        't', 'n' -> "S"
        'k', 'ň' -> "C"
        'p', 'm' -> "F"
        else -> ""
    }

    if (conf matches "..[SCF]".toRegex()) index++

    configuration = Configuration.byAbbreviation(conf) ?: return null

    if (normal.getOrNull(index) in setOf('s', 'š', 'f', 'ţ', 'ç')) {
        extension = when (normal[index]) {
            's' -> Extension.PROXIMAL
            'š' -> Extension.INCIPIENT
            'f' -> Extension.ATTENUATIVE
            'ţ' -> Extension.GRADUATIVE
            'ç' -> Extension.DEPLETIVE
            else -> return null
        }
        index++
    }

    if (normal.getOrNull(index) in setOf('d', 'g', 'b', 't', 'k', 'p')) {
        affiliation = when (normal[index]) {
            't', 'd' -> Affiliation.ASSOCIATIVE
            'k', 'g' -> Affiliation.COALESCENT
            'p', 'b' -> Affiliation.VARIATIVE
            else -> return null
        }
        index++
    }

    if (normal.drop(index).isNotEmpty() && index > 0) {
        perspective = when(normal[index]) {
            'r', 'v', 'l' -> Perspective.POLYADIC
            'w', 'm', 'h' -> Perspective.NOMIC
            'y', 'n', 'ç' -> Perspective.ABSTRACT
            else -> return null
        }
        essence = when(normal[index]) {
            'ř', 'l', 'm', 'h', 'n', 'ç' -> Essence.REPRESENTATIVE
            else -> Essence.NORMAL
        }
        index++
    }
    return if (normal.drop(index).isNotEmpty()) null else {
        Slot(configuration, extension, affiliation, perspective, essence)
    }
}


fun affixAdjunctScope(s: String?, scopingAdjunctVowel: Boolean = false): PrecisionString? {
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

    return scope?.let { PrecisionString(it, ignorable = default) }
}

fun parseSuppletiveAdjuncts(typeC: String, caseV: String, precision: Int, ignoreDefault: Boolean) : String? {

    val type = when(typeC.defaultForm()) {
        "hl" ->  "[carrier]"
        "hm" -> "[quotative]"
        "hn" -> "[naming]"
        "hr" -> "[phrasal]"
        else -> null
    }

    val case = Case.byVowel(caseV.defaultForm())?.toString(precision, ignoreDefault)

    return if (type != null && case != null) {
        type + (if (case.isNotEmpty()) "$SLOT_SEPARATOR$case" else "")
    } else null
}