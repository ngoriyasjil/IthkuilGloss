package io.github.syst3ms.tnil

fun wordTypeOf(groups: Array<String>) : WordType = when {
    groups.size == 1 && groups[0].isConsonant() ->  WordType.BIAS_ADJUNCT

    groups[0] in setOf("hl", "hm", "hn", "hň") && (groups.size == 2) -> WordType.SUPPLETIVE_ADJUNCT

    groups[0] == "hr" && (groups.size == 2) -> WordType.MOOD_CASESCOPE_ADJUNCT

    groups[0] == "h" && groups.size == 2 -> WordType.REGISTER_ADJUNCT

    (groups[0].isVowel() || groups[0] in setOf("w", "y"))
            && groups.all { it.isVowel() || it in CN_CONSONANTS } -> WordType.MODULAR_ADJUNCT

    groups.size >= 4 && groups[0] == "ë" && groups[3] in COMBINATION_PRA_SPECIFICATION
            || groups.size >= 3 && groups[0] !in CC_CONSONANTS && groups[2] in COMBINATION_PRA_SPECIFICATION
    -> WordType.COMBINATION_PRA

    groups.size in 2..3 && groups[1].isConsonant() && groups[1] !in CN_CONSONANTS
            || groups.size in 3..4 && groups[0] == "h" && groups[1] == "ë" -> WordType.AFFIXUAL_ADJUNCT

    groups.size >= 5 && groups[0].isConsonant() && groups[2] in CZ_CONSONANTS
            || groups.size >= 6 && (groups[0] == "ë") && (groups[3] in CZ_CONSONANTS) -> WordType.AFFIXUAL_SCOPING_ADJUNCT

    (groups.last().isVowel() || groups.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.last()?.isVowel() == true )
            && groups.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.dropLast(1)?.all { it.isConsonant() || it == "ë" } == true
    -> WordType.PERSONAL_REFERENCE_ADJUNCT

    else -> WordType.FORMATIVE
}

class ConcatenationChain(private vararg val formatives: Gloss) : Gloss() {

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return formatives
            .map {
                it.toString(precision, ignoreDefault)
            }
            .filter(String::isNotEmpty)
            .joinToString(CONCATENATION_SEPARATOR)
    }
}


fun parseConcatenationChain(s: String) : GlossOutcome {
    return s.split('-')
        .takeIf {
            it.all { word -> wordTypeOf(word.splitGroups()) == WordType.FORMATIVE }
        }.let { it ?: return Error("Non-formatives concatenated") }
        .map { parseWord(it) }
        .map {
            when (it) {
                is Gloss -> it
                is Error -> return it
            }
        }.let { ConcatenationChain(*it.toTypedArray()) }

}

fun parseWord(s: String) : GlossOutcome {

    val nonIthkuil = s.defaultForm().filter { it.toString() !in ITHKUIL_CHARS }
    if (nonIthkuil.isNotEmpty()) {
        return Error(
            "Non-ithkuil characters detected: " +
                    nonIthkuil.map { "\"$it\" (" + it.toInt().toString(16) + ")" }.joinToString() +
                    if (nonIthkuil.contains("[qˇ^ʰ]".toRegex())) " You might be writing in Ithkuil III. Try \"!gloss\" instead." else ""
        )
    }

    if ('-' in s) {
        return parseConcatenationChain(s)
    }

    val stress = s.substituteAll(ALLOGRAPHS).splitGroups().findStress()

    val (groups, sentencePrefix) = s.defaultForm().splitGroups().stripSentencePrefix() ?: return Error("Empty word")

    val result : GlossOutcome = when (wordTypeOf(groups)) {
        WordType.BIAS_ADJUNCT -> Gloss(Bias.byGroup(groups[0]) ?:  return Error("Unknown bias: ${groups[0]}"))

        WordType.SUPPLETIVE_ADJUNCT -> parseSuppletiveAdjuncts(groups[0], groups[1])

        WordType.MOOD_CASESCOPE_ADJUNCT -> parseMoodCaseScopeAdjunct(groups[1])

        WordType.REGISTER_ADJUNCT -> parseRegisterAdjunct(groups[1])

        WordType.MODULAR_ADJUNCT -> parseModular(groups, stress)

        WordType.COMBINATION_PRA -> parseCombinationPRA(groups, stress)

        WordType.AFFIXUAL_ADJUNCT -> parseAffixual(groups, stress)

        WordType.AFFIXUAL_SCOPING_ADJUNCT -> parseAffixualScoping(groups, stress)

        WordType.PERSONAL_REFERENCE_ADJUNCT -> parsePRA(groups, stress)

        WordType.FORMATIVE -> parseFormative(groups, stress)
    }

    return if (sentencePrefix) {
        when (result) {
            is Gloss -> result.addPrefix(sentenceStartGloss)
            is Error -> result
        }
    } else result

}

fun parseRegisterAdjunct(v: String): GlossOutcome {
    val (register, final) = Register.byVowel(v) ?: return Error("Unknown register adjunct vowel: $v")
    return Gloss(RegisterAdjunct(register, final))
}

fun parseFormative(groups: Array<String>, stress: Int) : GlossOutcome {

    var index = 0

    val (concatenation, shortcut) = if (groups[0] in CC_CONSONANTS) {
        index++
        parseCc(groups[0])
    } else Pair(null, null)

    val relation = if (concatenation == null) {
        when (stress){
            2 -> Relation.FRAMED
            else -> Relation.UNFRAMED
        }
    } else null

    val (vv, slotVFilled) = if (index == 0 && groups[0].isConsonant()) {
        "a" to false
    } else {
        unGlottalVowel(groups[index])?.also { index++ } ?: return Error("Unknown Vv vowel: ${groups[index]}")
    }

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

    val root : Glossable = when (rootMode) {
        RootMode.ROOT -> {
            val stem = slotII.getStem() ?: return Error("No stem found: $vv")
            Root(groups[index], stem).also { slotII.stemAvailable = it.hasStem }
        }
        RootMode.AFFIX -> {
            val vx = bySeriesAndForm(1, seriesAndForm(groups[index + 1]).second)
                ?: if (groups[index + 1] in setOf("üa", "üe", "üo", "üö")) {
                    "üa"
                } else
                    return Error("Unknown Cs-root Vr value: ${groups[index+1]}")
            Affix(vx, groups[index], noType = true)
        }
        RootMode.REFERENCE -> {
            parseFullReferent(groups[index]) ?: return Error("Unknown personal reference cluster: ${groups[index]}")
        }

    }
    index++

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

            if (vx.length == 3 && vx[1] == '\'' && indexV + 1 != groups.lastIndex) return Error("Unexpected glottal stop in slot V")

            csVxAffixes.add(Affix(cs = groups[indexV], vx = vx))
            indexV += 2


        }

        index = indexV

    }

    if (!slotVFilled && csVxAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")
    if (slotVFilled && csVxAffixes.size < 2) return Error("Unexpectedly few slot V affixes")

    if (csVxAffixes.size == 1) csVxAffixes[0].canBePraShortcut = true


    var cnInVI = false

    val slotVI = if (shortcut == null) {
        val ca = if (groups.getOrNull(index)?.isGeminateCa()
                ?: return Error("Formative ended before Ca")
        ) {
            if (csVxAffixes.isNotEmpty()) {
                groups[index].unGeminateCa()
            } else return Error("Unexpected glottal Ca: ${groups[index]}")
        } else groups[index]

        if (ca !in setOf("hl", "hr", "hm", "hn", "hň")) {
            parseCa(ca).also { index++ } ?: return Error("Unknown Ca value: $ca")
        } else {
            parseCa("l")!!.also{ cnInVI = true }
        }
    } else null

    val vxCsAffixes : MutableList<Glossable> = mutableListOf()

    if (!cnInVI) {
        while (true) {
            if (index+1 >= groups.size || groups[index+1] in CN_CONSONANTS || groups[index+1] == "-") {
                break
            }

            val (vx, glottalVowel) = unGlottalVowel(groups[index])
                ?: return Error("Unknown vowelform: ${groups[index]} (slot VII)")

            val glottalCs = groups[index+1].startsWith("'")

            vxCsAffixes.add(Affix(vx, groups[index+1].removePrefix("'")))
            index += 2

            if (glottalVowel || glottalCs) {
                vxCsAffixes.add(GlossString("{end of slot V}", "{Ca}"))

                if (slotVFilled && vxCsAffixes.size < 2) return Error("Unexpectedly few slot V affixes")
                else if (!slotVFilled && csVxAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")
            }
        }
    }

    if (vxCsAffixes.size == 1) (vxCsAffixes[0] as? Affix)?.canBePraShortcut = true

    val marksMood = (stress == 0) || (stress == -1)

    val slotVIII: Slot? = when {
        cnInVI -> {
            parseVnCn("a", groups[index], marksMood).also { index++ }
                ?: return Error("Unknown Cn value in Ca: ${groups[index]}")
        }
        groups.getOrNull(index+1) in CN_CONSONANTS -> {
            parseVnCn(groups[index], groups[index + 1], marksMood).also { index += 2 }
                ?: return Error("Unknown VnCn value: ${groups[index] + groups[index + 1]}")
        }
        else -> null
    }


    val vcVk = groups.getOrNull(index) ?: "a"

    val slotIX : Glossable = if (concatenation == null) {
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

    val slotList: List<Glossable> = listOfNotNull(relation, concatenation, slotII, root, slotIV) +
            csVxAffixes + listOfNotNull(slotVI) + vxCsAffixes + listOfNotNull(slotVIII, slotIX)

    return Gloss(*slotList.toTypedArray())

}



fun parseAffixVr(vr: String): Slot? {
    val (series, form) = seriesAndForm(vr)
        .let {
            if (it == Pair(-1,-1)) {
                val zeroSeries = when (vr) {
                    "üa" -> 1
                    "üe" -> 2
                    "üo" -> 3
                    "üö" -> 4
                    else -> return null
                }
                 zeroSeries to 0
            } else it
        }

    if (form !in 0..9) return null

    val degree = GlossString("degree $form", "D$form")

    val specification = when (series) {
        1 -> Specification.BASIC
        2 -> Specification.CONTENTIAL
        3 -> Specification.CONSTITUTIVE
        4 -> Specification.OBJECTIVE
        else -> return null
    }

    return Slot(degree, specification)
}

fun parseModular(groups: Array<String>, stress: Int) : GlossOutcome {

    var index = 0

    val slot1 = when (groups[0]) {
        "w" -> GlossString("{parent formative only}", "{parent}")
        "y" -> GlossString("{concatenated formative only}", "{concat.}")
        else -> null
    }

    if (slot1 != null) index++

    val midSlotList : MutableList<Slot> = mutableListOf()

    while (groups.size > index + 2) {
        midSlotList.add(
            parseVnCn(groups[index], groups[index + 1], false)
                ?: return Error("Unknown VnCn: ${groups[index]}${groups[index + 1]}")
        )
        index += 2
    }

    if (midSlotList.size > 3) return Error("Too many (>3) middle slots in modular adjunct: ${midSlotList.size}")

    val slot5 = when {
        midSlotList.isEmpty() -> Aspect.byVowel(groups[index]) ?: return Error("Unknown aspect: ${groups[index]}")
        stress == 1 -> parseVnCn(groups[index], "h", marksMood = true)
            ?: return Error("Unknown non-aspect Vn: ${groups[index]}")
        stress == 0 -> parseVh(groups[index]) ?: return Error("Unknown Vh: ${groups[index]}")
        else -> return Error("Unknown stress on modular adjunct: $stress from ultimate")
    }

    return Gloss(slot1, *midSlotList.toTypedArray(), slot5)

}

fun parsePRA(groups: Array<String>, stress: Int) : GlossOutcome {
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
            val vc2 = groups.getOrNull(index) ?: return Error("PRA ended unexpectedly")
            val caseB =
                Case.byVowel(vc2) ?: return Error("Unknown case: ${groups[index]}")
            index++

            val c2 = groups.getOrNull(index)
            val refB = if (c2 != null) {
                parseFullReferent(c2) ?: return Error("Unknown personal reference cluster: $c2")
            } else null

            index++
            if (groups.getOrNull(index) == "ë") index++

            if (groups.size > index) return Error("PRA is too long")

            return Gloss(refA, caseA, caseB, refB, essence)

        }
        groups.size > index + 1 -> return Error("PRA is too long")

        else -> return Gloss(refA, caseA, essence)
    }
}

fun parseCombinationPRA(groups: Array<String>, stress: Int): GlossOutcome {
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
        else -> return Error("Unknown combination PRA specification: ${groups[index]}")
    }
    index++

    val vxCsAffixes : MutableList<Glossable> = mutableListOf()
    while (true) {
        if (index+1 >= groups.size || groups[index+1] in CN_CONSONANTS || groups[index+1] == "-") {
            break
        }

        val (vx, glottal) = unGlottalVowel(groups[index])
            ?: return Error("Unknown vowelform: ${groups[index]} (slot VII)")

        if (glottal) return Error("Unexpected glottal stop")

        vxCsAffixes.add(Affix(vx, groups[index+1]))
        index += 2

    }

    val caseB = when (groups.getOrNull(index)?.defaultForm()) {
        "a", null -> null
        "üa" -> Case.THEMATIC
        else -> Case.byVowel(groups[index]) ?: return Error("Unknown case: ${groups[index]}")
    }

    return Gloss(ref, caseA, specification, *vxCsAffixes.toTypedArray(), caseB, essence)

}

fun parseAffixualScoping(groups: Array<String>, stress: Int): GlossOutcome {
    val concatOnly = if (stress == 0) GlossString("{concatenated formative only}","{concat.}") else null
    var index = 0
    if (groups[0] == "ë") index++
    val firstAffix = Affix(groups[index+1], groups[index])
    index += 2
    val scopeOfFirst = affixualAdjunctScope(groups[index]) ?: return Error("Unknown Cz: ${groups[index]}")
    index++

    val vxCsAffixes : MutableList<Glossable> = mutableListOf()

    while (true) {
        if (index+1 > groups.lastIndex) break

        val (vx, glottal) = unGlottalVowel(groups[index]) ?: return Error("Unknown vowelform: ${groups[index]}")

        if (glottal) return Error("Unexpected glottal stop in affixual scoping adjunct")

        vxCsAffixes.add(Affix(vx, groups[index+1]))
        index += 2
    }

    if (vxCsAffixes.isEmpty()) return Error("Only one affix found in affixual scoping adjunct")

    val vz = groups.getOrNull(index)

    val scopeOfRest = if (vz != null) {
        affixualAdjunctScope(vz, isMultipleAdjunctVowel = true) ?: return Error("Unknown Vz: $vz")
    } else null

    return Gloss(firstAffix, scopeOfFirst, *vxCsAffixes.toTypedArray(), scopeOfRest, concatOnly)

}


fun parseAffixual(groups: Array<String>, stress: Int) : GlossOutcome {
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

    val affix = Affix(groups[index], groups[index+1])
    val scope = affixualAdjunctScope(groups.getOrNull(index+2))

    return Gloss(affix, scope, concatOnly)

}


