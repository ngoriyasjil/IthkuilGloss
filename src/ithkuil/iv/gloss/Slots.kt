package ithkuil.iv.gloss

// Formative slots

fun parseCc(cc: String): Pair<Concatenation?, Shortcut?> {
    val concatenation = when (cc) {
        "h", "hl", "hm" -> Concatenation.TYPE_ONE
        "hw", "hr", "hn" -> Concatenation.TYPE_TWO
        else -> null
    }

    val shortcut = when (cc) {
        "w", "hl", "hr" -> Shortcut.W
        "y", "hm", "hn" -> Shortcut.Y
        else -> null
    }

    return Pair(concatenation, shortcut)
}

private fun caOf(
    affiliation: Affiliation = Affiliation.CONSOLIDATIVE,
    configuration: Configuration = Configuration.UNIPLEX,
    extension: Extension = Extension.DELIMITIVE,
    perspective: Perspective = Perspective.MONADIC,
    essence: Essence = Essence.NORMAL,
): Slot {
    return Slot(affiliation, configuration, extension, perspective, essence)
}

fun parseVv(vv: String, shortcut: Shortcut?): Slot? {
    return if (vv in SPECIAL_VV_VOWELS) {
        parseSpecialVv(vv, shortcut)
    } else {
        parseNormalVv(vv, shortcut)
    }
}

fun parseNormalVv(v: String, shortcut: Shortcut?): Slot? {

    val (series, form) = seriesAndForm(v)

    val stem = when (form) {
        1, 2 -> Stem.ONE
        3, 4 -> Stem.TWO
        9, 8 -> Stem.THREE
        7, 6 -> Stem.ZERO
        else -> return null
    }.let {
        Underlineable(it)
    }


    val version = when (form) {
        1, 3, 9, 7 -> Version.PROCESSUAL
        2, 4, 8, 6 -> Version.COMPLETIVE
        else -> return null
    }

    val additional: Glossable

    when (shortcut) {
        null -> {
            additional = when (series) {
                1 -> Slot()
                2 -> CsAffix("r", Degree.FOUR)
                3 -> CsAffix("t", Degree.FOUR)
                4 -> CsAffix("t", Degree.FIVE)
                else -> return null
            }
        }

        Shortcut.W -> {
            additional = when (series) {
                1 -> caOf()
                2 -> caOf(perspective = Perspective.AGGLOMERATIVE)
                3 -> caOf(perspective = Perspective.NOMIC)
                4 -> caOf(perspective = Perspective.AGGLOMERATIVE, essence = Essence.REPRESENTATIVE)
                else -> return null
            }
        }

        Shortcut.Y -> {
            additional = when (series) {
                1 -> caOf(extension = Extension.PROXIMAL)
                2 -> caOf(essence = Essence.REPRESENTATIVE)
                3 -> caOf(perspective = Perspective.ABSTRACT)
                4 -> caOf(extension = Extension.PROXIMAL, essence = Essence.REPRESENTATIVE)
                else -> return null
            }
        }
    }

    return Slot(stem, version, additional)

}

fun parseSpecialVv(vv: String, shortcut: Shortcut?): Slot? {
    val version = when (vv) {
        "ëi", "eë", "ae" -> Version.PROCESSUAL
        "ëu", "oë", "ea" -> Version.COMPLETIVE
        else -> return null
    }

    val function = when (vv) {
        "ëi", "ëu" -> Function.STATIVE
        "eë", "oë" -> Function.DYNAMIC
        else -> null
    }

    val ca = if (shortcut != null && vv in setOf("ae", "ea")) {
        when (shortcut) {
            Shortcut.W -> caOf()
            Shortcut.Y -> caOf(extension = Extension.PROXIMAL)
        }
    } else if (shortcut != null) {
        return null
    } else null

    return Slot(version, function, ca)

}

fun parseVr(vr: String): Slot? {
    val (series, form) = seriesAndForm(vr)

    val specification = when (form) {
        1, 9 -> Specification.BASIC
        2, 8 -> Specification.CONTENTIAL
        3, 7 -> Specification.CONSTITUTIVE
        4, 6 -> Specification.OBJECTIVE
        else -> return null
    }
    val function = when (form) {
        1, 2, 3, 4 -> Function.STATIVE
        9, 8, 7, 6 -> Function.DYNAMIC
        else -> return null
    }

    val context = when (series) {
        1 -> Context.EXISTENTIAL
        2 -> Context.FUNCTIONAL
        3 -> Context.REPRESENTATIONAL
        4 -> Context.AMALGAMATIVE
        else -> return null
    }

    return Slot(function, specification, context)

}

fun parseAffixVr(vr: String): Slot? {
    val (series, form) = seriesAndForm(vr)
        .let {
            if (it == Pair(-1, -1)) {
                val zeroSeries = when (vr) {
                    "ae" -> 1
                    "ea" -> 2
                    "äi" -> 3
                    "öi" -> 4
                    else -> return null
                }
                zeroSeries to 0
            } else it
        }

    val degree = Degree.byForm(form) ?: return null

    val specification = when (series) {
        1 -> Specification.BASIC
        2 -> Specification.CONTENTIAL
        3 -> Specification.CONSTITUTIVE
        4 -> Specification.OBJECTIVE
        else -> return null
    }

    return Slot(degree, specification)
}

fun String.isGeminateCa(): Boolean = when {
    withIndex().any { (index, ch) -> ch == getOrNull(index + 1) } -> true
    this in CA_DEGEMINATIONS.keys -> true
    else -> false
}

fun String.isOvergeminatedCa(): Boolean = zipWithNext { a, b -> a == b }.count { it } >= 2

fun String.degeminateCa(): String {
    val allomorph = CA_DEGEMINATIONS.keys.find { this.endsWith(it) }
    return when {
        allomorph != null -> replace(allomorph, CA_DEGEMINATIONS[allomorph]!!)
        zipWithNext().any { (a, b) -> a == b } ->
            zipWithNext { a, b -> if (a != b) b else "" }
                .joinToString("", prefix = take(1))

        else -> this
    }
}

data class CaForms(
    val affiliation: String,
    val configuration: String,
    val extension: String,
    val perspectiveAndEssence: String,
) {
    fun affiliationStandalone() =
        configuration.isEmpty() &&
            extension.isEmpty() &&
            perspectiveAndEssence.isEmpty()

    fun perspectiveAndEssenceStandalone() =
        affiliation.isEmpty() &&
            configuration.isEmpty() &&
            extension.isEmpty()
}

fun gatherCaValues(ca: String): CaForms? {
    val affiliation = "[nrř]ļ\$|[lrř](?=.)".toRegex()
    val configuration = "[stckpţfçšzčžẓ]|[kpţf]s|[kp]š".toRegex()
    val extension = "(?<=^[lrř]?)(?:[gb]z|d)|[tkpgb]".toRegex()
    val perspectiveEssence = "^[lvj]|^tļ|[lrwyřmhnç]".toRegex()

    val fullRegex = "($affiliation)?($configuration)?($extension)?($perspectiveEssence)?".toRegex()

    val matches = fullRegex
        .matchEntire(ca)
        ?.groups
        ?.drop(1)
        ?.map { it?.value ?: "" } ?: return null

    return CaForms(matches[0], matches[1], matches[2], matches[3])
}

fun parseCa(ca: String): Slot? {

    val unwoundCa = ca.substituteAll(CA_DESUBSTITUTIONS)

    val forms = gatherCaValues(unwoundCa) ?: return null

    val affiliation = if (forms.affiliationStandalone()) {
        when (forms.affiliation) {
            "" -> Affiliation.CONSOLIDATIVE
            "nļ" -> Affiliation.ASSOCIATIVE
            "rļ" -> Affiliation.COALESCENT
            "řļ" -> Affiliation.VARIATIVE
            else -> return null
        }
    } else {
        when (forms.affiliation) {
            "" -> Affiliation.CONSOLIDATIVE
            "l" -> Affiliation.ASSOCIATIVE
            "r" -> Affiliation.COALESCENT
            "ř" -> Affiliation.VARIATIVE
            else -> return null
        }
    }

    val configuration = when (forms.configuration) {
        "" -> Configuration.UNIPLEX
        "s" -> Configuration.DUPLEX
        "t" -> Configuration.MULTIPLEX_SIMILAR_SEPARATE
        "c" -> Configuration.DUPLEX_SIMILAR_SEPARATE
        "k" -> Configuration.MULTIPLEX_SIMILAR_CONNECTED
        "ks" -> Configuration.DUPLEX_SIMILAR_CONNECTED
        "p" -> Configuration.MULTIPLEX_SIMILAR_FUSED
        "ps" -> Configuration.DUPLEX_SIMILAR_FUSED
        "ţ" -> Configuration.MULTIPLEX_DISSIMILAR_SEPARATE
        "ţs" -> Configuration.DUPLEX_DISSIMILAR_SEPARATE
        "f" -> Configuration.MULTIPLEX_DISSIMILAR_CONNECTED
        "fs" -> Configuration.DUPLEX_DISSIMILAR_CONNECTED
        "ç" -> Configuration.MULTIPLEX_DISSIMILAR_FUSED
        "š" -> Configuration.DUPLEX_DISSIMILAR_FUSED
        "z" -> Configuration.MULTIPLEX_FUZZY_SEPARATE
        "č" -> Configuration.DUPLEX_FUZZY_SEPARATE
        "ž" -> Configuration.MULTIPLEX_FUZZY_CONNECTED
        "kš" -> Configuration.DUPLEX_FUZZY_CONNECTED
        "ẓ" -> Configuration.MULTIPLEX_FUZZY_FUSED
        "pš" -> Configuration.DUPLEX_FUZZY_FUSED
        else -> return null
    }

    val extension = if (forms.configuration.isEmpty()) {
        when (forms.extension) {
            "" -> Extension.DELIMITIVE
            "d" -> Extension.PROXIMAL
            "g" -> Extension.INCEPTIVE
            "b" -> Extension.ATTENUATIVE
            "gz" -> Extension.GRADUATIVE
            "bz" -> Extension.DEPLETIVE
            else -> return null
        }
    } else {
        when (forms.extension) {
            "" -> Extension.DELIMITIVE
            "t" -> Extension.PROXIMAL
            "k" -> Extension.INCEPTIVE
            "p" -> Extension.ATTENUATIVE
            "g" -> Extension.GRADUATIVE
            "b" -> Extension.DEPLETIVE
            else -> return null
        }
    }

    val (perspective, essence) = if (forms.perspectiveAndEssenceStandalone()) {
        when (forms.perspectiveAndEssence) {
            "l" -> Perspective.MONADIC to Essence.NORMAL
            "r" -> Perspective.AGGLOMERATIVE to Essence.NORMAL
            "v" -> Perspective.NOMIC to Essence.NORMAL
            "j" -> Perspective.ABSTRACT to Essence.NORMAL
            "tļ" -> Perspective.MONADIC to Essence.REPRESENTATIVE
            "ř" -> Perspective.AGGLOMERATIVE to Essence.REPRESENTATIVE
            "m", "h" -> Perspective.NOMIC to Essence.REPRESENTATIVE
            "n", "ç" -> Perspective.ABSTRACT to Essence.REPRESENTATIVE
            else -> return null
        }
    } else {
        when (forms.perspectiveAndEssence) {
            "" -> Perspective.MONADIC to Essence.NORMAL
            "r" -> Perspective.AGGLOMERATIVE to Essence.NORMAL
            "w" -> Perspective.NOMIC to Essence.NORMAL
            "y" -> Perspective.ABSTRACT to Essence.NORMAL
            "l" -> Perspective.MONADIC to Essence.REPRESENTATIVE
            "ř" -> Perspective.AGGLOMERATIVE to Essence.REPRESENTATIVE
            "m", "h" -> Perspective.NOMIC to Essence.REPRESENTATIVE
            "n", "ç" -> Perspective.ABSTRACT to Essence.REPRESENTATIVE
            else -> return null
        }
    }

    return Slot(affiliation, configuration, extension, perspective, essence)
}

fun parseVnCn(vn: String, cn: String, marksMood: Boolean = true, absoluteLevel: Boolean = false): Slot? {

    if (cn !in CN_CONSONANTS) return null

    val (series, form) = seriesAndForm(vn)

    if (absoluteLevel && (series != 4 || cn !in CN_PATTERN_ONE)) return null

    val vnValue: Glossable = if (cn in CN_PATTERN_ONE) {
        when (series) {
            1 -> Valence.byForm(form)
            2 -> Phase.byForm(form)
            3 -> EffectAndPerson.byForm(form)
            4 -> LevelAndRelativity(form, absoluteLevel)
            else -> return null
        }
    } else {
        Aspect.byVowel(vn) ?: return null
    }

    val cnValue: Glossable = when (cn) {
        "h", "w", "y" -> Mood.FACTUAL to CaseScope.NATURAL
        "hl", "hw" -> Mood.SUBJUNCTIVE to CaseScope.ANTECEDENT
        "hr", "hlw" -> Mood.ASSUMPTIVE to CaseScope.SUBALTERN
        "hm", "hly" -> Mood.SPECULATIVE to CaseScope.QUALIFIER
        "hn", "hnw" -> Mood.COUNTERFACTIVE to CaseScope.PRECEDENT
        "hň", "hny" -> Mood.HYPOTHETICAL to CaseScope.SUCCESSIVE
        else -> return null
    }.let {
        if (marksMood) it.first else it.second
    }

    return Slot(vnValue, cnValue)

}

fun parseVk(vk: String, inIveAffix: Boolean = false): Slot? {
    val (series, form) = if (vk !in setOf("ae", "ea", "äi")) {
        seriesAndForm(vk)
    } else {
        if (!inIveAffix) return null
        when (vk) {
            "ae" -> 1 to 0
            "ea" -> 2 to 0
            "äi" -> 3 to 0
            else -> return null
        }
    }

    val illocution = when (form) {
        5 -> Illocution.PERFORMATIVE
        else -> Illocution.ASSERTIVE
    }

    val expectation = when (series) {
        1 -> Expectation.COGNITIVE
        2 -> Expectation.RESPONSIVE
        3 -> Expectation.EXECUTIVE
        else -> return null
    }
    val validation = when (form) {
        0 -> null
        1 -> Validation.OBSERVATIONAL
        2 -> Validation.RECOLLECTIVE
        3 -> Validation.PURPORTIVE
        4 -> Validation.REPORTIVE
        5 -> null
        6 -> Validation.IMAGINARY
        7 -> Validation.CONVENTIONAL
        8 -> Validation.INTUITIVE
        9 -> Validation.INFERENTIAL
        else -> return null
    }
    return Slot(illocution, expectation, validation)
}

// Referentials

fun parseSingleReferent(r: String): Slot? {
    val referent: Category = when (r) {
        "tļ" -> Perspective.AGGLOMERATIVE
        "ç", "x" -> Perspective.NOMIC
        "w", "y" -> Perspective.ABSTRACT
        else -> Referent.byForm(r) ?: return null
    }

    val effect = when (r) {
        "l", "s", "n", "m", "ň", "z", "ẓ", "ļ", "c", "th", "ll", "mm" -> Effect.NEUTRAL
        "r", "š", "t", "p", "k", "ţ", "f", "č", "ph", "rr", "nn" -> Effect.BENEFICIAL
        "ř", "ž", "d", "b", "g", "ḑ", "v", "j", "kh", "řř", "ňň" -> Effect.DETRIMENTAL
        else -> null
    }

    return Slot(referent, effect)
}

fun parseFullReferent(clusters: List<String>): Referential? {

    val reflist: List<Slot> = clusters.flatMap { c ->
        parseFullReferent(c) ?: return null
    }

    return when (reflist.size) {
        0 -> null
        else -> Referential(reflist)
    }
}

fun parseFullReferent(c: String): Referential? {
    val referents = buildList {
        var index = 0

        while (index <= c.lastIndex) {

            val referent = if (index + 2 <= c.length && c.substring(index, index + 2) in BICONSONANTAL_REFERENTIALS) {
                parseSingleReferent(c.substring(index, index + 2)).also { index += 2 }
            } else {
                parseSingleReferent(c.substring(index, index + 1)).also { index++ }
            }

            if (referent != null) add(referent)
        }

    }

    return when (referents.size) {
        0 -> null
        else -> Referential(referents)
    }

}

// Adjunct slots

fun parseVh(vh: String): GlossString? = when (vh) {
    "a" -> GlossString("{scope over formative}", "{form.}")
    "e" -> GlossString("{scope over case/mood}", "{mood}")
    "i", "u" -> GlossString("{scope over formative, but not adjacent adjuncts}", "{under adj.}")
    "o" -> GlossString("{scope over formative and adjacent adjuncts}", "{over adj.}")
    else -> null
}

fun affixualAdjunctScope(vsCzVz: String?, isMultipleAdjunctVowel: Boolean = false): GlossString? {
    val scope = when (vsCzVz) {
        null -> if (isMultipleAdjunctVowel) "{same}" else "{VDom}"
        "h", "a" -> "{VDom}"
        "'h", "u" -> "{VSub}"
        "'hl", "e" -> "{VIIDom}"
        "'hr", "i" -> "{VIISub}"
        "hw", "o" -> "{formative}"
        "'hw", "ö" -> "{adjacent}"
        "ai" -> if (isMultipleAdjunctVowel) "{same}" else null
        else -> null
    }
    val isDefaultForm = if (isMultipleAdjunctVowel) scope == "{same}" else scope == "{VDom}"

    return scope?.let { GlossString(it, ignorable = isDefaultForm) }
}


