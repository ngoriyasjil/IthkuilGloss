package ithkuil.iv.gloss

import ithkuil.iv.gloss.dispatch.logger

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
            WordType.BIAS_ADJUNCT             -> parseBiasAdjunct           (word)
            WordType.MOOD_CASESCOPE_ADJUNCT   -> parseMoodCaseScopeAdjunct  (word)
            WordType.REGISTER_ADJUNCT         -> parseRegisterAdjunct       (word)
            WordType.MODULAR_ADJUNCT          -> parseModular               (word, marksMood = marksMood)
            WordType.COMBINATION_REFERENTIAL  -> parseCombinationReferential(word)
            WordType.AFFIXUAL_ADJUNCT         -> parseAffixual              (word)
            WordType.MULTIPLE_AFFIX_ADJUNCT   -> parseMultipleAffix         (word)
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

    return ConcatenationChain(glosses)
}

fun parseBiasAdjunct(word: Word) : GlossOutcome {
    return Bias.byGroup(word[0])?.let { Gloss(it) } ?: Error("Unknown bias: ${word[0]}")
}


fun parseRegisterAdjunct(word: Word): GlossOutcome {
    val (register, final) = Register.byVowel(word[1]) ?: return Error("Unknown register adjunct vowel: ${word[1]}")
    return Gloss(RegisterAdjunct(register, final))
}



@OptIn(ExperimentalStdlibApi::class)
@Suppress("UNCHECKED_CAST")
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

            val stem = slotII.find { it is Underline<*> && it.value is Stem } as? Underline<Stem> ?: return Error("Stem not present")
            Root(groups[index], stem)
        }
        RootMode.AFFIX -> {
            val form = if (groups[index+1] in DEGREE_ZERO_CS_ROOT_FORMS) 0 else seriesAndForm(groups[index+1]).second
            val degree = Degree.values().find { it.short == form } ?: return Error("Unknown Cs-root degree: $form")
            CsAffix(groups[index], degree)
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



    val slotV : List<ValidAffix> = csVxAffixes.parseAll().map { when(it) {
        is AffixError -> return Error(it.message)
        is ValidAffix -> it }
    }


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
            DEFAULT_CA.also { cnInVI = true }
        }
    } else null)?.let { if (csVxAffixes.isNotEmpty()) ForcedDefault(it, "{Ca}") else it }

    val vxCsAffixes: MutableList<Affix> = mutableListOf()

    var hasSlotV = false

    var caIndex : Int? = null

    val endOfSlotVMarker = GlossString("{end of slot V}", "{Ca}")

    while (true) {
        if (index + 1 > groups.lastIndex || groups[index + 1] in CN_CONSONANTS) {
            break
        }

        val vx = groups[index]

        vxCsAffixes.add(Affix(vx, groups[index + 1]))


        if (shortcut != null && (index in glottalIndices)) {
            if (slotVFilled && vxCsAffixes.size < 2) return Error("Unexpectedly few slot V affixes")
            else if (!slotVFilled && vxCsAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")

            caIndex = vxCsAffixes.size

            hasSlotV = true
        }

        index += 2

    }


    if (shortcut != null && slotVFilled && !hasSlotV) return Error("Unexpectedly few slot V affixes")

    val slotVIIAndMaybeSlotV : List<Glossable> = vxCsAffixes.parseAll().map { when(it) {
        is AffixError -> return Error(it.message)
        is ValidAffix -> it }
    }.let { if (caIndex != null) {
        buildList {
            addAll(it.subList(0, caIndex))
            add(endOfSlotVMarker)
            addAll(it.subList(caIndex, it.size))
        }
    } else it
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
            slotV + listOfNotNull(slotVI) + slotVIIAndMaybeSlotV + listOfNotNull(slotVIII, slotIX)

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

    val vxCsAffixes: MutableList<ValidAffix> = mutableListOf()
    while (true) {
        if (index + 1 > word.lastIndex) {
            break
        }

        val affix = Affix(word[index], word[index + 1]).parse().let {
            when(it) {
                is AffixError -> return Error(it.message)
                is ValidAffix -> it
            }
        }

        vxCsAffixes.add(affix)
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

    val firstAffixVx = word[index+1].removeSuffix("'")

    val czGlottal = firstAffixVx != word[index+1]

    val firstAffix = Affix(cs = word[index], vx = firstAffixVx).parse().let {
        when(it) {
            is AffixError -> return Error(it.message)
            is ValidAffix -> it
        }
    }
    index += 2

    val cz = "${if (czGlottal) "'" else ""}${word[index]}"

    val scopeOfFirst = affixualAdjunctScope(cz) ?: return Error("Unknown Cz: ${word[index]}")
    index++

    val vxCsAffixes: MutableList<ValidAffix> = mutableListOf()

    while (true) {
        if (index + 1 > word.lastIndex) break

        val affix = Affix(word[index], word[index + 1]).parse().let {
            when(it) {
                is AffixError -> return Error(it.message)
                is ValidAffix -> it
            }
        }

        vxCsAffixes.add(affix)
        index += 2
    }

    if (vxCsAffixes.isEmpty()) return Error("Only one affix found in multiple affix adjunct")

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

    val affix = Affix(word[0], word[1]).parse().let {
        when(it) {
            is AffixError -> return Error(it.message)
            is ValidAffix -> it
        }
    }

    val scope = affixualAdjunctScope(word.getOrNull(2))

    return Gloss(affix, scope, stressMarked = concatOnly)

}

fun parseMoodCaseScopeAdjunct(word: Word) : GlossOutcome {
    val value : Glossable = when (val v = word[1]) {
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