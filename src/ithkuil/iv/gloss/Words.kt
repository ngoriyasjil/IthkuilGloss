package ithkuil.iv.gloss

import ithkuil.iv.gloss.dispatch.logger

fun wordTypeOf(word: Word): WordType {

    return when {
        word.size == 1 && word[0].isConsonant() -> WordType.BIAS_ADJUNCT

        word[0] == "hr" && word.size == 2 -> WordType.MOOD_CASESCOPE_ADJUNCT

        word[0] == "h" && word.size == 2 -> WordType.REGISTER_ADJUNCT

        (word[0].isVowel() || word[0] in setOf("w", "y"))
            && word.all { it.isVowel() || it in CN_CONSONANTS } -> WordType.MODULAR_ADJUNCT

        word.size >= 4 && word[0] == "ë" && word[3] in COMBINATION_REFERENTIAL_SPECIFICATION
            || word.size >= 3 && word[0] !in CC_CONSONANTS && word[2] in COMBINATION_REFERENTIAL_SPECIFICATION
            || word.size >= 4 && word[0] == "ï" && word[1] in CP_CONSONANTS
        -> WordType.COMBINATION_REFERENTIAL

        word.size in 2..3 && word[1].isConsonant() && word[1] !in CN_CONSONANTS && word[0] != "ë"
        -> WordType.AFFIXUAL_ADJUNCT

        word.size >= 5 && word[0].isConsonant() && word[2] in CZ_CONSONANTS
            || word.size >= 6 && (word[0] == "ë") && (word[3] in CZ_CONSONANTS)
        -> WordType.AFFIXUAL_SCOPING_ADJUNCT

        (word.last().isVowel() || word.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.last()
            ?.isVowel() == true)
            && word.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.dropLast(1)
            ?.all { it.isConsonant() || it == "ë" } == true
        -> WordType.REFERENTIAL

        else -> WordType.FORMATIVE
    }
}

fun parseWithoutSentencePrefix(word: Word, parseFunction: (Word) -> GlossOutcome) : GlossOutcome {
    val (strippedWord, sentencePrefix) = word.stripSentencePrefix()

    val result = parseFunction(strippedWord)

    return when {
        sentencePrefix && result is Gloss -> result.addPrefix(SENTENCE_START_GLOSS)
        else -> result
    }
}

fun parseWord(iword: Word, marksMood : Boolean? = null): GlossOutcome {
    logger.info { "-> parseWord($iword)" }

    val result = parseWithoutSentencePrefix(iword) { word ->
        when (wordTypeOf(word)) {
            WordType.BIAS_ADJUNCT             -> parseBiasAdjunct(word[0])
            WordType.MOOD_CASESCOPE_ADJUNCT   -> parseMoodCaseScopeAdjunct  (word[1])
            WordType.REGISTER_ADJUNCT         -> parseRegisterAdjunct       (word[1])
            WordType.MODULAR_ADJUNCT          -> parseModular               (word, marksMood = marksMood)
            WordType.COMBINATION_REFERENTIAL  -> parseCombinationReferential(word)
            WordType.AFFIXUAL_ADJUNCT         -> parseAffixual              (word)
            WordType.AFFIXUAL_SCOPING_ADJUNCT -> parseMultipleAffix         (word)
            WordType.REFERENTIAL              -> parseReferential           (word)
            WordType.FORMATIVE                -> parseFormative             (word)
        }
    }
    logger.info {
        "   parseWord($iword) -> " +
            when (result) {
                is Gloss -> "Gloss(${result.toString(GlossOptions(Precision.SHORT))})"
                is Error -> "Error(${result.message})"
                is Foreign -> "Impossible foreign word: ${result.word})"
            }
    }

    return result

}

fun parseConcatenationChain(chain: ConcatenatedWords): GlossOutcome {

    for ((index, word) in chain.words.withIndex()) {
        if (word.isEmpty())
            return Error("Empty word concatenated (at ${index + 1})")

        if (wordTypeOf(word) != WordType.FORMATIVE)
            return Error("Non-formatives concatenated (at ${index + 1})")

        val (concatenation, _) = parseCc(word[0])

        if ((concatenation == null) xor (index == chain.words.lastIndex))
            return Error("Invalid concatenation (at ${index + 1})")
    }

    val glosses = chain.words.map {
        val gloss = parseWithoutSentencePrefix(it) {
                formative -> parseFormative(formative, inConcatenationChain = true)
        }

        when (gloss) {
            is Gloss -> gloss
            is Error -> return gloss
            is Foreign -> throw IllegalArgumentException()
        }
    }

    return ConcatenationChain(*glosses.toTypedArray())
}

fun parseBiasAdjunct(cb: String) : GlossOutcome {
    return Bias.byGroup(cb)?.let { Gloss(it) } ?: Error("Unknown bias: $cb")
}


fun parseRegisterAdjunct(v: String): GlossOutcome {
    val (register, final) = Register.byVowel(v) ?: return Error("Unknown register adjunct vowel: $v")
    return Gloss(RegisterAdjunct(register, final))
}

fun parseFormative(word: Word, inConcatenationChain: Boolean = false) : GlossOutcome {
    val glottalIndices = word.mapIndexedNotNull { index, group ->
        if (group.contains('\'')) index else null
    }

    if (glottalIndices.size > 2) return Error("Too many glottal stops found")

    val groups = word.map { group ->
        if (group.contains('\'')) {
            when {
                group.isVowel() -> unGlottalVowel(group)?.first ?: group
                else -> return Error("Unexpected glottal stop: $group")
            }
        } else group
    }

    var index = 0

    val (concatenation, shortcut) = if (groups[0] in CC_CONSONANTS) {
        index++
        parseCc(groups[0])
    } else Pair(null, null)

    if (!inConcatenationChain && concatenation != null) return Error("Lone concatenated formative")

    val relation = if (concatenation == null) {
        when (word.stress) {
            Stress.ANTEPENULTIMATE -> Relation.FRAMED
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
            val stem = slotII.find { it is Stem } as? Stem ?: return Error("Stem not present")
            Root(groups[index], Underline(stem))
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

    if (concatenation != null && caseGlottal) return Error("Unexpected glottal stop in concatenated formative")

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

    val slotVI = (if (shortcut == null) {
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
    } else null)?.let { if (csVxAffixes.isNotEmpty()) ForcedDefault(it, "{Ca}") else it }

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
                if (slotVFilled && vxCsAffixes.size < 2) return Error("Unexpectedly few slot V affixes")
                else if (!slotVFilled && vxCsAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")

                vxCsAffixes.add(GlossString("{end of slot V}", "{Ca}"))

                hasSlotV = true
            }

            index += 2

        }
    }

    if (shortcut != null && slotVFilled && !hasSlotV) return Error("Unexpectedly few slot V affixes")

    if (vxCsAffixes.size == 1) {
            (vxCsAffixes[0] as? Affix)?.canBeReferentialShortcut = true
    }

    val marksMood = word.stress in setOf(Stress.ULTIMATE, Stress.MONOSYLLABIC)

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
            glottalVowel(it)?.first ?: return Error("Unknown slot IX vowel: $it")
        } else it
    }


    val slotIX: Glossable = if (concatenation == null) {
        when (word.stress) {
            Stress.MONOSYLLABIC, Stress.ULTIMATE -> parseVk(vcVk) ?: return Error("Unknown Vk form $vcVk")
            Stress.PENULTIMATE, Stress.ANTEPENULTIMATE -> Case.byVowel(vcVk) ?: return Error("Unknown Vc form $vcVk")
            else -> return Error("Stress error during formative parsing. Please report")
        }
    } else {
        when (word.stress) {
            Stress.PENULTIMATE -> Case.byVowel(vcVk) ?: return Error("Unknown Vf form $vcVk (penultimate stress)")
            Stress.MONOSYLLABIC, Stress.ULTIMATE -> {
                val glottalified = when (vcVk.length) {
                    1 -> "$vcVk'$vcVk"
                    2 -> "${vcVk[0]}'${vcVk[1]}"
                    else -> return Error("Vf form is too long: $vcVk")
                }
                Case.byVowel(glottalified) ?: return Error("Unknown Vf form $vcVk (ultimate stress)")
            }
            Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress in concatenated formative")
            else -> return Error("Stress error")
        }
    }
    index++

    if (groups.lastIndex >= index) return Error("Formative continued unexpectedly: ${groups[index]}")

    val slotList: List<Glossable> = listOfNotNull(concatenation, slotII, root, slotIV) +
            csVxAffixes + listOfNotNull(slotVI) + vxCsAffixes + listOfNotNull(slotVIII, slotIX)

    return Gloss(*slotList.toTypedArray(), stressMarked = relation)

}

fun parseModular(word: Word, marksMood: Boolean?): GlossOutcome {

    var index = 0

    val slot1 = when (word[0]) {
        "w" -> GlossString("{parent formative only}", "{parent}")
        "y" -> GlossString("{concatenated formative only}", "{concat.}")
        else -> null
    }

    if (slot1 != null) index++

    val midSlotList: MutableList<Slot> = mutableListOf()

    while (word.size > index + 2) {
        midSlotList.add(
            parseVnCn(word[index], word[index + 1], marksMood = marksMood ?: true, absoluteLevel = false)
                ?: return Error("Unknown VnCn: ${word[index]}${word[index + 1]}")
        )
        index += 2
    }

    if (midSlotList.size > 3) return Error("Too many (>3) middle slots in modular adjunct: ${midSlotList.size}")

    val slot5 = when {
        midSlotList.isEmpty() -> Aspect.byVowel(word[index]) ?: return Error("Unknown aspect: ${word[index]}")
        else -> when (word.stress) {
            Stress.PENULTIMATE -> parseVnCn(word[index], "h", marksMood = true, absoluteLevel = false)
                ?: return Error("Unknown non-aspect Vn: ${word[index]}")
            Stress.ULTIMATE -> parseVh(word[index]) ?: return Error("Unknown Vh: ${word[index]}")
            Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress on modular adjunct")
            else -> return Error("Stress error")
        }

    }

    return Gloss(slot1, *midSlotList.toTypedArray(), slot5)

}

val BICONSONANTAL_PRS = setOf("th", "ph", "kh", "ll", "rr", "řř", "hl", "hm", "hn", "hň")

class Referential(private vararg val referents: Slot) : Glossable, Iterable<Slot> {
    override fun toString(o: GlossOptions): String {
        return when (referents.size) {
            0 -> ""
            1 -> referents[0].toString(o)
            else -> referents
                .joinToString(REFERENT_SEPARATOR, REFERENT_START, REFERENT_END)
                { it.toString(o) }
        }
    }

    override fun iterator(): Iterator<Slot> = referents.iterator()


}

fun parseFullReferent(clusters: List<String>): Referential? {

    val reflist : List<Slot> = clusters.flatMap { c ->
        parseFullReferent(c) ?: return null
    }

    return when (reflist.size) {
        0 -> null
        else -> Referential(*reflist.toTypedArray())
    }
}

private fun parseFullReferent(c: String): Referential? {
    val referents = sequence {
        var index = 0

        while (index <= c.lastIndex) {

            val referent = if (index + 2 <= c.length && c.substring(index, index + 2) in BICONSONANTAL_PRS) {
                parseSingleReferent(c.substring(index, index + 2)).also { index += 2 }
            } else {
                parseSingleReferent(c.substring(index, index + 1)).also { index++ }
            }

            if (referent != null) yield(referent)
        }

    }.toList()

    return when (referents.size) {
        0 -> null
        else -> Referential(*referents.toTypedArray())
    }

}

fun parseReferential(word: Word): GlossOutcome {
    val essence = when (word.stress) {
        Stress.ULTIMATE -> Essence.REPRESENTATIVE
        Stress.MONOSYLLABIC, Stress.PENULTIMATE -> Essence.NORMAL
        Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress on referential")
        else -> return Error("Stress error")
    }
    val c1 = word
        .takeWhile { it !in setOf("w", "y") }
        .dropLast(1)
        .filter { it != "ë" }
        .takeIf { it.size <= 3 } ?: return Error("Too many (>3) initial ë-separated consonants")
    val refA =
        parseFullReferent(c1) ?: return Error("Unknown personal reference: $c1")
    var index = (word
        .indexOfFirst { it in setOf("w", "y") }
        .takeIf { it != -1 } ?: word.size) - 1

    val caseA = Case.byVowel(word[index]) ?: return Error("Unknown case: ${word[index]}")
    index++

    when {
        word.getOrNull(index) in setOf("w", "y") -> {
            index++
            val vc2 = word.getOrNull(index) ?: return Error("Referential ended unexpectedly")
            val caseB =
                Case.byVowel(vc2) ?: return Error("Unknown case: ${word[index]}")
            index++

            val c2 = word.getOrNull(index)
            val refB = if (c2 != null) {
                parseFullReferent(c2) ?: return Error("Unknown personal reference cluster: $c2")
            } else null

            index++
            if (word.getOrNull(index) == "ë") index++

            if (word.size > index) return Error("Referential is too long")

            return Gloss(refA, Shown(caseA), Shown(caseB), refB, stressMarked = essence)

        }
        word.size > index + 1 -> return Error("Referential is too long")

        else -> return Gloss(refA, caseA, stressMarked = essence)
    }
}

fun parseCombinationReferential(word: Word): GlossOutcome {
    val essence = when (word.stress) {
        Stress.ULTIMATE -> Essence.REPRESENTATIVE
        Stress.MONOSYLLABIC, Stress.PENULTIMATE -> Essence.NORMAL
        Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress on combination referential")
        else -> return Error("Stress error")
    }
    var index = 0

    if (word[0] in setOf("ë", "ï")) {
        if ((word[0] == "ï") != (word[1] in CP_CONSONANTS)) return Error("Epenthetic ï must only be used with Suppletive forms")
        index++
    }


    val ref = parseFullReferent(word[index]) ?: return Error("Unknown referent: ${word[index]}")

    index++

    val caseA = Case.byVowel(word[index]) ?: return Error("Unknown case: ${word[index]}")
    index++

    val specification = when (word[index]) {
        "x" -> Specification.BASIC
        "xx" -> Specification.CONTENTIAL
        "lx" -> Specification.CONSTITUTIVE
        "rx" -> Specification.OBJECTIVE
        else -> return Error("Unknown combination referential Specification: ${word[index]}")
    }
    index++

    val vxCsAffixes: MutableList<Glossable> = mutableListOf()
    while (true) {
        if (index + 1 > word.lastIndex) {
            break
        }

        vxCsAffixes.add(Affix(word[index], word[index + 1]))
        index += 2

    }

    val caseB = when (word.getOrNull(index)) {
        "a", null -> null
        "üa" -> Case.THEMATIC
        else -> Case.byVowel(word[index]) ?: return Error("Unknown case: ${word[index]}")
    }


    return Gloss(ref, Shown(caseA, condition = caseB != null), specification,
        *vxCsAffixes.toTypedArray(),
        caseB?.let { Shown(it) }, stressMarked = essence)

}

fun parseMultipleAffix(word: Word): GlossOutcome {
    val concatOnly = if (word.stress == Stress.ULTIMATE) {
        GlossString("{concatenated formative only}", "{concat.}")
    } else null
    var index = 0
    if (word[0] == "ë") index++
    val firstAffix = Affix(word[index + 1], word[index])
    index += 2
    val scopeOfFirst = affixualAdjunctScope(word[index]) ?: return Error("Unknown Cz: ${word[index]}")
    index++

    val vxCsAffixes: MutableList<Glossable> = mutableListOf()

    while (true) {
        if (index + 1 > word.lastIndex) break

        val (vx, glottal) = unGlottalVowel(word[index]) ?: return Error("Unknown vowelform: ${word[index]}")

        if (glottal) return Error("Unexpected glottal stop in affixual scoping adjunct")

        vxCsAffixes.add(Affix(vx, word[index + 1]))
        index += 2
    }

    if (vxCsAffixes.isEmpty()) return Error("Only one affix found in affixual scoping adjunct")

    val vz = word.getOrNull(index)

    val scopeOfRest = if (vz != null) {
        affixualAdjunctScope(vz, isMultipleAdjunctVowel = true) ?: return Error("Unknown Vz: $vz")
    } else null

    return Gloss(firstAffix, scopeOfFirst, *vxCsAffixes.toTypedArray(), scopeOfRest, stressMarked = concatOnly)

}


fun parseAffixual(word: Word): GlossOutcome {
    val concatOnly = if (word.stress == Stress.ULTIMATE)
        GlossString("{concatenated formative only}", "{concat.}")
    else null

    if (word.size < 2) return Error("Affixual adjunct too short: ${word.size}")

    val affix = Affix(word[0], word[1])
    val scope = affixualAdjunctScope(word.getOrNull(2))

    return Gloss(affix, scope, stressMarked = concatOnly)

}

fun parseMoodCaseScopeAdjunct(v: String) : GlossOutcome {
    val value : Glossable = when (v) {
        "a" -> Mood.FACTUAL
        "e" -> Mood.SUBJUNCTIVE
        "i" -> Mood.ASSUMPTIVE
        "ö" -> Mood.SPECULATIVE
        "o" -> Mood.COUNTERFACTIVE
        "u" -> Mood.HYPOTHETICAL
        "ai" -> CaseScope.NATURAL
        "ei" -> CaseScope.ANTECEDENT
        "iu" -> CaseScope.SUBALTERN
        "ëi" -> CaseScope.QUALIFIER
        "oi" -> CaseScope.PRECEDENT
        "ui" -> CaseScope.SUCCESSIVE
        else -> return Error("Unknown Mood/Case-Scope adjunct vowel: $v")
    }

    return Gloss(Shown(value))
}