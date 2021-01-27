package ithkuil.iv.gloss

fun wordTypeOf(groups: Array<String>): WordType = when {
    groups.size == 1 && groups[0].isConsonant() -> WordType.BIAS_ADJUNCT

    groups[0] in setOf("hl", "hm", "hn", "hň") && (groups.size == 2) -> WordType.SUPPLETIVE_ADJUNCT

    groups[0] == "hr" && (groups.size == 2) -> WordType.MOOD_CASESCOPE_ADJUNCT

    groups[0] == "h" && groups.size == 2 -> WordType.REGISTER_ADJUNCT

    (groups[0].isVowel() || groups[0] in setOf("w", "y"))
            && groups.all { it.isVowel() || it in CN_CONSONANTS } -> WordType.MODULAR_ADJUNCT

    groups.size >= 4 && groups[0] == "ë" && groups[3] in COMBINATION_REFERENTIAL_SPECIFICATION
            || groups.size >= 3 && groups[0] !in CC_CONSONANTS && groups[2] in COMBINATION_REFERENTIAL_SPECIFICATION
    -> WordType.COMBINATION_REFERENTIAL

    groups.size in 2..3 && groups[1].isConsonant() && groups[1] !in CN_CONSONANTS
            || groups.size in 3..4 && groups[0] == "h" && groups[1] == "ë" -> WordType.AFFIXUAL_ADJUNCT

    groups.size >= 5 && groups[0].isConsonant() && groups[2] in CZ_CONSONANTS
            || groups.size >= 6 && (groups[0] == "ë") && (groups[3] in CZ_CONSONANTS) -> WordType.AFFIXUAL_SCOPING_ADJUNCT

    (groups.last().isVowel() || groups.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.last()
        ?.isVowel() == true)
            && groups.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.dropLast(1)
        ?.all { it.isConsonant() || it == "ë" } == true
    -> WordType.REFERENTIAL

    else -> WordType.FORMATIVE
}

fun parseWord(s: String): GlossOutcome {
    logger.info { "-> parseWord($s)" }
    val nonIthkuil = s.defaultForm().filter { it.toString() !in ITHKUIL_CHARS }
    if (nonIthkuil.isNotEmpty()) {
        return Error(
            "Non-ithkuil characters detected: " +
                    nonIthkuil.map { "\"$it\" (U+" + it.toInt().toString(16).toUpperCase().padStart(4, '0') + ")" }
                        .joinToString() +
                    if (nonIthkuil.contains("[qˇ^ʰ]".toRegex())) " You might be writing in Ithkuil III. Try \"!gloss\" instead." else ""
        )
    }

    if ('-' in s) {
        return parseConcatenationChain(s)
    }

    val stress = s.substituteAll(ALLOGRAPHS).splitGroups().findStress() ?: return Error("Unknown stress")

    val (groups, sentencePrefix) = s.defaultForm().splitGroups().stripSentencePrefix() ?: return Error("Empty word")

    val result: GlossOutcome = when (wordTypeOf(groups)) {
        WordType.BIAS_ADJUNCT             -> Gloss(Bias.byGroup(groups[0]) ?: return Error("Unknown bias: ${groups[0]}"))
        WordType.SUPPLETIVE_ADJUNCT       -> parseSuppletiveAdjuncts    (groups[0], groups[1])
        WordType.MOOD_CASESCOPE_ADJUNCT   -> parseMoodCaseScopeAdjunct  (groups[1])
        WordType.REGISTER_ADJUNCT         -> parseRegisterAdjunct       (groups[1])
        WordType.MODULAR_ADJUNCT          -> parseModular               (groups, stress)
        WordType.COMBINATION_REFERENTIAL  -> parseCombinationReferential(groups, stress)
        WordType.AFFIXUAL_ADJUNCT         -> parseAffixual              (groups, stress)
        WordType.AFFIXUAL_SCOPING_ADJUNCT -> parseMultipleAffix         (groups, stress)
        WordType.REFERENTIAL              -> parseReferential           (groups, stress)
        WordType.FORMATIVE                -> parseFormative             (groups, stress)
    }

    return when {
        sentencePrefix && result is Gloss -> result.addPrefix(SENTENCE_START_GLOSS)
        else -> result
    }.also {
        logger.info {
            ("   parseWord($s) -> " + when (it) {
                is Gloss -> "Gloss(${it.toString(GlossOptions(Precision.SHORT))})"
                is Error -> "Error(${it.message})"
            })
        }
    }
}

fun parseConcatenationChain(s: String): GlossOutcome {
    return s.split('-')
        .takeIf { it.all { word -> word.isNotEmpty() } }
        .let { it ?: return Error("Empty word concatenated") }
        .takeIf { it.all { word -> wordTypeOf(word.defaultForm().splitGroups()) == WordType.FORMATIVE } }
        .let { it ?: return Error("Non-formatives concatenated") }
        .map(::parseWord)
        .map { it as? Gloss ?: return it }
        .let { ConcatenationChain(*it.toTypedArray()) }
}

fun parseRegisterAdjunct(v: String): GlossOutcome {
    val (register, final) = Register.byVowel(v) ?: return Error("Unknown register adjunct vowel: $v")
    return Gloss(RegisterAdjunct(register, final))
}

fun parseFormative(igroups: Array<String>, stress: Int): GlossOutcome {
    val glottalIndices = igroups
        .mapIndexedNotNull { index, group -> if (group.contains('\'')) index else null }

    if (glottalIndices.size > 2) return Error("Too many glottal stops found")

    val groups = igroups.map { group ->
        if (group.contains('\'')) {
            when {
                group.isVowel() -> unGlottalVowel(group)?.first ?: group
                group.startsWith('\'') && group.count { it == '\'' } == 1 -> group.drop(1)
                else -> return Error("Unexpected glottal stop: $group")
            }
        } else group
    }

    var index = 0

    val (concatenation, shortcut) = if (groups[0] in CC_CONSONANTS) {
        index++
        parseCc(groups[0])
    } else Pair(null, null)


    val relation = if (concatenation == null) {
        when (stress) {
            2 -> Relation.FRAMED
            else -> Relation.UNFRAMED
        }
    } else null

    val slotVFilled = groups[index].isVowel() && (index in glottalIndices || index + 1 in glottalIndices)

    val vv: String = if (index == 0 && groups[0].isConsonant()) "a" else groups[index].also { index++ }

    var rootMode = RootMode.ROOT

    val slotII = if (vv in SPECIAL_VV_VOWELS) {
        when (vv) {
            "ëi", "eë", "ëu", "öë" -> {
                rootMode = RootMode.AFFIX
                if (shortcut != null) return Error("Shortcuts can't be used with a Cs-root")
            }
            "eä", "öä" -> rootMode = RootMode.REFERENCE
        }
        parseSpecialVv(vv, shortcut) ?: return Error("Unknown Vv value: $vv")
    } else parseVv(vv, shortcut) ?: return Error("Unknown Vv value: $vv")

    val root: Glossable = when (rootMode) {
        RootMode.ROOT -> {
            val stem = slotII.getStem() ?: return Error("No stem found: $vv")
            Root(groups[index], stem).also { slotII.stemAvailable = it.hasStem }
        }
        RootMode.AFFIX -> {
            val vx = bySeriesAndForm(1, seriesAndForm(groups[index + 1]).second)
                ?: if (groups[index + 1] in setOf("üa", "üe", "üo", "üö")) {
                    "üa"
                } else
                    return Error("Unknown Cs-root Vr value: ${groups[index + 1]}")
            Affix(vx, groups[index], noType = true)
        }
        RootMode.REFERENCE -> {
            parseFullReferent(groups[index]) ?: return Error("Unknown personal reference cluster: ${groups[index]}")
        }

    }
    index++

    val caseGlottal = if (shortcut == null) {
        glottalIndices.any { it in (index)..(groups.lastIndex) }
    } else groups.last().isVowel() && groups.lastIndex in glottalIndices

    if (concatenation != null && caseGlottal) return Error("Unexpected glottal stop in incorporated formative")

    val vr = if (shortcut != null) "a" else {
        groups.getOrNull(index).also { index++ }
            ?: return Error("Formative ended unexpectedly: ${groups.joinToString("")}")
    }

    val slotIV = when (rootMode) {
        RootMode.ROOT, RootMode.REFERENCE -> parseVr(vr) ?: return Error("Unknown Vr value: $vr")
        RootMode.AFFIX -> parseAffixVr(vr) ?: return Error("Unknown Cs-root Vr value: $vr")
    }

    val csVxAffixes: MutableList<Affix> = mutableListOf()

    if (shortcut == null) {
        var indexV = index
        while (true) {
            if (groups.getOrNull(indexV)?.isGeminateCa() == true) {
                break
            } else if (indexV + 1 > groups.lastIndex || groups[indexV] in CN_CONSONANTS || groups[indexV] == "-") {
                csVxAffixes.clear()
                indexV = index
                break
            }

            val vx = groups[indexV + 1]

            if (!vx.isVowel()) return Error("Unknown vowelform: $vx")

            csVxAffixes.add(Affix(cs = groups[indexV], vx = vx))
            indexV += 2


        }

        index = indexV

        if (!slotVFilled && csVxAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")
        if (slotVFilled && csVxAffixes.size < 2) return Error("Unexpectedly few slot V affixes")

    }

    if (csVxAffixes.size == 1) csVxAffixes[0].canBeReferentialShortcut = true


    var cnInVI = false

    val slotVI = if (shortcut == null) {
        val ca = if (groups.getOrNull(index)?.isGeminateCa() ?: return Error("Formative ended before Ca")) {
            if (csVxAffixes.isNotEmpty()) {
                groups[index].unGeminateCa()
            } else return Error("Unexpected glottal Ca: ${groups[index]}")
        } else groups[index]

        if (ca !in setOf("hl", "hr", "hm", "hn", "hň")) {
            parseCa(ca).also { index++ } ?: return Error("Unknown Ca value: $ca")
        } else {
            parseCa("l")!!.also { cnInVI = true }
        }
    } else null

    if (csVxAffixes.isNotEmpty()) slotVI?.default = "{Ca}"

    val vxCsAffixes: MutableList<Glossable> = mutableListOf()

    var hasSlotV = false

    if (!cnInVI) {
        while (true) {
            if (index + 1 > groups.lastIndex || groups[index + 1] in CN_CONSONANTS) {
                break
            }

            val vx = groups[index]

            vxCsAffixes.add(Affix(vx, groups[index + 1].removePrefix("'")))


            if (shortcut != null && (index in glottalIndices || index + 1 in glottalIndices)) {
                vxCsAffixes.add(GlossString("{end of slot V}", "{Ca}"))

                if (slotVFilled && vxCsAffixes.size < 2) return Error("Unexpectedly few slot V affixes")
                else if (!slotVFilled && csVxAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")

                hasSlotV = true
            }

            index += 2

        }
    }

    if (shortcut != null && slotVFilled && !hasSlotV) return Error("Unexpectedly few slot V affixes")

    if (vxCsAffixes.size == 1) {
            (vxCsAffixes[0] as? Affix)?.canBeReferentialShortcut = true
    }

    val marksMood = (stress == 0) || (stress == -1)


    val absoluteLevel = groups.getOrNull(index + 1) == "y" &&
            groups.getOrNull(index + 3) in setOf("h", "hl", "hr", "hm", "hn", "hň")

    val slotVIII: Slot? = when {
        cnInVI -> {
            parseVnCn("a", groups[index], marksMood, false).also { index++ }
                ?: return Error("Unknown Cn value in Ca: ${groups[index]}")
        }

        absoluteLevel -> {
                    parseVnCn(groups[index] + groups[index+2],
                        groups[index + 3],
                        marksMood,
                        absoluteLevel = true)
                            .also { index += 4 }

                        ?: return Error("Unknown VnCn value: ${groups
                            .subList(index, index + 4)
                            .joinToString("")
                        }")
                }

        groups.getOrNull(index + 1) in CN_CONSONANTS -> {
            parseVnCn(groups[index], groups[index + 1], marksMood, false).also { index += 2 }
                ?: return Error("Unknown VnCn value: ${groups[index] + groups[index + 1]}")
        }
        else -> null
    }

    val vcVk = (groups.getOrNull(index) ?: "a").let {
        if (caseGlottal) {
            if (groups.lastIndex < index) return Error("Unexpected glottal stop with elided Vc")
            glottalVowel(it)?.first ?: return Error("Unknown slot IX vowel: $it")
        } else it
    }


    val slotIX: Glossable = if (concatenation == null) {
        when (stress) {
            0, -1 -> parseVk(vcVk) ?: return Error("Unknown Vk form $vcVk")
            1, 2 -> Case.byVowel(vcVk) ?: return Error("Unknown Vc form $vcVk")
            else -> return Error("Unknown stress: $stress from ultimate")
        }
    } else {
        when (stress) {
            1 -> Case.byVowel(vcVk) ?: return Error("Unknown Vf form $vcVk (penultimate stress)")
            0 -> {
                val glottalified = when (vcVk.length) {
                    1 -> "$vcVk'$vcVk"
                    2 -> "${vcVk[0]}'${vcVk[1]}"
                    else -> return Error("Vf form is too long: $vcVk")
                }
                Case.byVowel(glottalified) ?: return Error("Unknown Vf form $vcVk (ultimate stress)")
            }
            else -> return Error("Unknown stress for concatenated formative: $stress from ultimate")
        }
    }
    index++

    if (groups.lastIndex >= index) return Error("Formative continued unexpectedly: ${groups[index]}")

    val slotList: List<Glossable> = listOfNotNull(concatenation, slotII, root, slotIV) +
            csVxAffixes + listOfNotNull(slotVI) + vxCsAffixes + listOfNotNull(slotVIII, slotIX)

    return Gloss(*slotList.toTypedArray(), stressMarked = relation)

}

fun parseModular(groups: Array<String>, stress: Int): GlossOutcome {

    var index = 0

    val slot1 = when (groups[0]) {
        "w" -> GlossString("{parent formative only}", "{parent}")
        "y" -> GlossString("{concatenated formative only}", "{concat.}")
        else -> null
    }

    if (slot1 != null) index++

    val midSlotList: MutableList<Slot> = mutableListOf()

    while (groups.size > index + 2) {
        midSlotList.add(
            parseVnCn(groups[index], groups[index + 1], false, false)
                ?: return Error("Unknown VnCn: ${groups[index]}${groups[index + 1]}")
        )
        index += 2
    }

    if (midSlotList.size > 3) return Error("Too many (>3) middle slots in modular adjunct: ${midSlotList.size}")

    val slot5 = when {
        midSlotList.isEmpty() -> Aspect.byVowel(groups[index]) ?: return Error("Unknown aspect: ${groups[index]}")
        stress == 1 -> parseVnCn(groups[index], "h", marksMood = true, absoluteLevel = false)
            ?: return Error("Unknown non-aspect Vn: ${groups[index]}")
        stress == 0 -> parseVh(groups[index]) ?: return Error("Unknown Vh: ${groups[index]}")
        else -> return Error("Unknown stress on modular adjunct: $stress from ultimate")
    }

    return Gloss(slot1, *midSlotList.toTypedArray(), slot5)

}

fun parseReferential(groups: Array<String>, stress: Int): GlossOutcome {
    val essence = if (stress == 0) Essence.REPRESENTATIVE else Essence.NORMAL
    val c1 = groups
        .takeWhile { it !in setOf("w", "y") }
        .filter { it != "ë" }
        .dropLast(1)
        .takeIf { it.size <= 6 }
        ?.joinToString("") ?: return Error("Too many (>3) initial ë-separated consonants")
    val refA =
        parseFullReferent(c1) ?: return Error("Unknown personal reference cluster: $c1")
    var index = (groups
        .indexOfFirst { it in setOf("w", "y") }
        .takeIf { it != -1 } ?: groups.size) - 1

    val caseA = Case.byVowel(groups[index]) ?: return Error("Unknown case: ${groups[index]}")
    index++

    when {
        groups.getOrNull(index) in setOf("w", "y") -> {
            index++
            val vc2 = groups.getOrNull(index) ?: return Error("Referential ended unexpectedly")
            val caseB =
                Case.byVowel(vc2) ?: return Error("Unknown case: ${groups[index]}")
            index++

            val c2 = groups.getOrNull(index)
            val refB = if (c2 != null) {
                parseFullReferent(c2) ?: return Error("Unknown personal reference cluster: $c2")
            } else null

            index++
            if (groups.getOrNull(index) == "ë") index++

            if (groups.size > index) return Error("Referential is too long")

            return Gloss(refA, caseA, caseB, refB, stressMarked = essence)

        }
        groups.size > index + 1 -> return Error("Referential is too long")

        else -> return Gloss(refA, caseA, stressMarked = essence)
    }
}

fun parseCombinationReferential(groups: Array<String>, stress: Int): GlossOutcome {
    val essence = if (stress == 0) Essence.REPRESENTATIVE else Essence.NORMAL
    var index = 0

    if (groups[0] == "ë") index++

    val ref = parseFullReferent(groups[index]) ?: return Error("Unknown referent: ${groups[index]}")

    index++

    val caseA = Case.byVowel(groups[index]) ?: return Error("Unknown case: ${groups[index]}")
    index++

    val specification = when (groups[index]) {
        "x" -> Specification.BASIC
        "xx" -> Specification.CONTENTIAL
        "lx" -> Specification.CONSTITUTIVE
        "rx" -> Specification.OBJECTIVE
        else -> return Error("Unknown combination referential Specification: ${groups[index]}")
    }
    index++

    val vxCsAffixes: MutableList<Glossable> = mutableListOf()
    while (true) {
        if (index + 1 > groups.lastIndex) {
            break
        }

        vxCsAffixes.add(Affix(groups[index], groups[index + 1]))
        index += 2

    }

    val caseB = when (groups.getOrNull(index)) {
        "a", null -> null
        "üa" -> Case.THEMATIC
        else -> Case.byVowel(groups[index]) ?: return Error("Unknown case: ${groups[index]}")
    }

    return Gloss(ref, caseA, specification, *vxCsAffixes.toTypedArray(), caseB, stressMarked = essence)

}

fun parseMultipleAffix(groups: Array<String>, stress: Int): GlossOutcome {
    val concatOnly = if (stress == 0) GlossString("{concatenated formative only}", "{concat.}") else null
    var index = 0
    if (groups[0] == "ë") index++
    val firstAffix = Affix(groups[index + 1], groups[index])
    index += 2
    val scopeOfFirst = affixualAdjunctScope(groups[index]) ?: return Error("Unknown Cz: ${groups[index]}")
    index++

    val vxCsAffixes: MutableList<Glossable> = mutableListOf()

    while (true) {
        if (index + 1 > groups.lastIndex) break

        val (vx, glottal) = unGlottalVowel(groups[index]) ?: return Error("Unknown vowelform: ${groups[index]}")

        if (glottal) return Error("Unexpected glottal stop in affixual scoping adjunct")

        vxCsAffixes.add(Affix(vx, groups[index + 1]))
        index += 2
    }

    if (vxCsAffixes.isEmpty()) return Error("Only one affix found in affixual scoping adjunct")

    val vz = groups.getOrNull(index)

    val scopeOfRest = if (vz != null) {
        affixualAdjunctScope(vz, isMultipleAdjunctVowel = true) ?: return Error("Unknown Vz: $vz")
    } else null

    return Gloss(firstAffix, scopeOfFirst, *vxCsAffixes.toTypedArray(), scopeOfRest, stressMarked = concatOnly)

}


fun parseAffixual(groups: Array<String>, stress: Int): GlossOutcome {
    val concatOnly = if (stress == 0)
        GlossString("{concatenated formative only}", "{concat.}")
    else null

    if (groups.size < 2) return Error("Affixual adjunct too short: ${groups.size}")

    var index = 0

    when {
        groups[0] == "h" && groups[1] == "ë" -> index++
        groups[0] == "h" && groups[1] != "ë" -> return Error("Non-degree 4 affixual adjuncts prefixed with h")
        groups[0] == "ë" -> return Error("Degree 4 affixual adjunct not prefixed with h")
    }

    val affix = Affix(groups[index], groups[index + 1])
    val scope = affixualAdjunctScope(groups.getOrNull(index + 2))

    return Gloss(affix, scope, stressMarked = concatOnly)

}


