package ithkuil.iv.gloss

import ithkuil.iv.gloss.dispatch.logger

// @formatter:off
@Suppress("Reformat") // This looks like dogshit without the vertical alignment.
fun parseWord(word: Word, marksMood: Boolean? = null): ParseOutcome {
    logger.info { "-> parseWord($word)" }

    val result = when (word.wordType) {
            WordType.BIAS_ADJUNCT            -> parseBiasAdjunct           (word)
            WordType.MOOD_CASESCOPE_ADJUNCT  -> parseMoodCaseScopeAdjunct  (word)
            WordType.REGISTER_ADJUNCT        -> parseRegisterAdjunct       (word)
            WordType.MODULAR_ADJUNCT         -> parseModular               (word, marksMood = marksMood)
            WordType.COMBINATION_REFERENTIAL -> parseCombinationReferential(word)
            WordType.AFFIXUAL_ADJUNCT        -> parseAffixual              (word)
            WordType.MULTIPLE_AFFIX_ADJUNCT  -> parseMultipleAffix         (word)
            WordType.REFERENTIAL             -> parseReferential           (word)
            WordType.FORMATIVE               -> parseFormative             (word)
        }.let {
            if (it is Parsed && word.hasSentencePrefix) {
                it.addPrefix(SENTENCE_START_GLOSS)
            } else it
    }

    logger.info {
        "   parseWord($word) -> " +
            when (result) {
                is Parsed -> "Gloss(${result.gloss(GlossOptions(Precision.SHORT))})"
                is Error -> "Error(${result.message})"
            }
    }

    return result

}
// @formatter:on

fun parseConcatenationChain(chain: ConcatenatedWords): ParseOutcome {

    for ((index, word) in chain.words.withIndex()) {
        val (concatenation, _) = parseCc(word[0])

        if ((concatenation == null) xor (index == chain.words.lastIndex))
            return Error("Invalid concatenation (at ${index + 1})")
    }

    val glosses = chain.words.mapIndexed { index, word ->

        val gloss = parseFormative(word, inConcatenationChain = true)

        val glossWithPrefix = if (gloss is Parsed && word.hasSentencePrefix) {
            if (index == 0) {
                gloss.addPrefix(SENTENCE_START_GLOSS)
            } else return Error("Sentence prefix inside concatenation chain")
        } else gloss

        when (glossWithPrefix) {
            is Parsed -> glossWithPrefix
            is Error -> return glossWithPrefix
        }
    }

    return ConcatenationChain(glosses)
}

fun parseBiasAdjunct(word: Word): ParseOutcome {
    val bias = Bias.byCb(word[0]) ?: return Error("Unknown bias: ${word[0]}")

    return Parsed(bias)
}


fun parseRegisterAdjunct(word: Word): ParseOutcome {
    val adjunct = Register.adjunctByVowel(word[1]) ?: return Error("Unknown register adjunct vowel: ${word[1]}")

    return Parsed(adjunct)
}


fun parseFormative(word: Word, inConcatenationChain: Boolean = false): ParseOutcome {

    val glottalIndices = mutableListOf<Int>()

    val groups = word.mapIndexed { index, group ->
        if ('\'' in group) {
            glottalIndices.add(index)
            unglottalizeVowel(group)
        } else group
    }

    var index = 0

    val (concatenation, shortcut) = if (groups[0] in CC_CONSONANTS) {
        index++
        parseCc(groups[0])
    } else Pair(null, null)

    val maxGlottalStops = if (shortcut == null) 2 else 3

    if (glottalIndices.size > maxGlottalStops) return Error("Too many glottal stops")

    if (!inConcatenationChain && concatenation != null) return Error("Lone concatenated formative")

    val relation = if (concatenation == null) {
        when (word.stress) {
            Stress.ANTEPENULTIMATE -> Relation.FRAMED
            else -> Relation.UNFRAMED
        }
    } else null

    val slotVFilledMarker = index in glottalIndices

    val vv: String = if (index == 0 && groups[0].isConsonant()) "a" else groups[index].also { index++ }

    val rootMode = when (vv) {
        "ëi", "eë", "ëu", "oë" -> RootMode.AFFIX
        "ae", "ea" -> RootMode.REFERENCE
        else -> RootMode.ROOT
    }

    if (rootMode == RootMode.AFFIX && shortcut != null) return Error("Shortcuts can't be used with a Cs-root")

    val slotII = parseVv(vv, shortcut) ?: return Error("Unknown Vv value: $vv")

    val cr = groups[index]

    if (cr.isInvalidRootForm()) return Error("Invalid root form: $cr")

    val root: Glossable = when (rootMode) {
        RootMode.ROOT -> {
            val stem = slotII.findIsInstance<Underlineable<Stem>>() ?: return Error("Stem not found")
            Root(cr, stem)
        }

        RootMode.AFFIX -> {
            val form = when (val affixVr = groups[index + 1]) {
                in DEGREE_ZERO_CS_ROOT_FORMS -> 0
                else -> seriesAndForm(affixVr).second
            }
            val degree = Degree.byForm(form) ?: return Error("Unknown Cs-root degree: $form")
            CsAffix(cr, degree)
        }

        RootMode.REFERENCE -> {
            parseFullReferent(cr) ?: return Error("Unknown personal reference cluster: $cr")
        }

    }
    index++

    val glottalShiftStartIndex = index

    val vr = if (shortcut != null) "a" else {
        groups.getOrNull(index).also { index++ } ?: return Error("Formative ended unexpectedly")
    }

    val slotIV = when (rootMode) {
        RootMode.ROOT, RootMode.REFERENCE -> parseVr(vr) ?: return Error("Unknown Vr value: $vr")
        RootMode.AFFIX -> parseAffixVr(vr) ?: return Error("Unknown Cs-root Vr value: $vr")
    }


    val csVxAffixes = if (shortcut == null) {
        var indexV = index

        buildList {
            while (true) {
                if (groups.getOrNull(indexV)?.isGeminateCa() == true) {
                    index = indexV
                    break
                } else if (indexV + 1 > groups.lastIndex || groups[indexV] in CN_CONSONANTS) {
                    clear()
                    indexV = index
                    break
                }
                add(Affix(cs = groups[indexV], vx = groups[indexV + 1]))
                indexV += 2
            }
        }

    } else emptyList()


    if (shortcut == null) {
        if (!slotVFilledMarker && csVxAffixes.size >= 2) return Error("Unexpectedly many slot V affixes")
        if (slotVFilledMarker && csVxAffixes.size < 2) return Error("Unexpectedly few slot V affixes")
    }


    val slotV = csVxAffixes.parseAll().validateAll { return Error(it.message) }

    val isVerbal = word.stress in setOf(Stress.ULTIMATE, Stress.MONOSYLLABIC)

    var cnMoved = false

    val slotVI = if (shortcut != null) null else {
        val caForm = groups.getOrNull(index) ?: return Error("Formative ended unexpectedly")

        val caValue = when {
            caForm in CN_PATTERN_ONE -> {
                if (caForm == "h") return Error("Unexpected default Cn in slot VI")
                cnMoved = true
                parseVnCn("a", caForm, isVerbal)
                    ?: return Error("Unknown Cn value in Ca: $caForm")
            }

            caForm.isGeminateCa() -> {
                if (caForm.isOvergeminatedCa()) return Error("Overgeminated Ca: $caForm")
                if (csVxAffixes.isEmpty()) return Error("Unexpected geminated Ca: $caForm")
                val ungeminated = caForm.degeminateCa()
                parseCa(ungeminated) ?: return Error("Unknown Ca value: $ungeminated")
            }

            else -> parseCa(caForm) ?: return Error("Unknown Ca value: $caForm")

        }

        index++
        ForcedDefault(caValue, "{Ca}", condition = csVxAffixes.isNotEmpty())
    }

    var endOfVxCsSlotVIndex: Int? = null

    val vxCsAffixes = buildList {
        while (true) {
            if (index + 1 > groups.lastIndex || groups[index + 1] in CN_CONSONANTS)
                break

            add(Affix(vx = groups[index], cs = groups[index + 1]))


            if (shortcut != null && index in glottalIndices) {

                if (slotVFilledMarker && size < 2) return Error("Unexpectedly few slot V affixes")
                else if (!slotVFilledMarker && size >= 2) return Error("Unexpectedly many slot V affixes")

                endOfVxCsSlotVIndex = size
            }

            index += 2
        }
    }

    if (shortcut != null && slotVFilledMarker && endOfVxCsSlotVIndex == null) return Error("Unexpectedly few slot V affixes")

    val endOfSlotVGloss = GlossString("{end of slot V}", "{Ca}")

    val slotVIIAndMaybeSlotV: List<Glossable> = vxCsAffixes
        .parseAll()
        .validateAll { return Error(it.message) }
        .let { affixList ->

            val endOfSlotVIndex = endOfVxCsSlotVIndex // Necessary for smart casting (KT-7186)

            if (endOfSlotVIndex != null) {
                buildList {
                    addAll(affixList.subList(0, endOfSlotVIndex))
                    add(endOfSlotVGloss)
                    addAll(affixList.subList(endOfSlotVIndex, affixList.size))
                }
            } else affixList
        }

    val absoluteLevel = groups.getOrNull(index + 1) == "y" &&
        groups.getOrNull(index + 3) in CN_PATTERN_ONE

    val slotVIII: Slot? = when {
        absoluteLevel -> {
            val vn = groups[index] + groups[index + 2]
            val cn = groups[index + 3]

            parseVnCn(
                vn,
                cn,
                marksMood = isVerbal,
                absoluteLevel = true
            ).also { index += 4 }
                ?: return Error("Unknown VnCn value: ${vn[0]}y${vn[1]}$cn")
        }

        groups.getOrNull(index + 1) in CN_CONSONANTS -> {
            parseVnCn(
                groups[index],
                groups[index + 1],
                marksMood = isVerbal
            )?.also { index += 2 }
                ?: return Error("Unknown VnCn value: ${groups[index] + groups[index + 1]}")
        }

        else -> null
    }

    val caseGlottal = if (shortcut == null) {
        if (cnMoved) {
            if (glottalIndices.any { it in glottalShiftStartIndex until groups.lastIndex }) {
                return Error("Unexpected glottal stop with a moved Cn")
            }
        }
        glottalIndices.any { it in glottalShiftStartIndex..(groups.lastIndex) }
    } else groups.last().isVowel() && groups.lastIndex in glottalIndices

    if (concatenation != null && caseGlottal) return Error("Unexpected glottal stop in concatenated formative")

    val vcVk = (groups.getOrNull(index) ?: "a")
        .let {
            if (caseGlottal) glottalizeVowel(it) else it
        }


    val slotIX: Glossable = if (concatenation != null) {
        when (word.stress) {
            Stress.PENULTIMATE -> Case.byVowel(vcVk)
                ?: return Error("Unknown Vf form $vcVk (penultimate stress)")

            Stress.MONOSYLLABIC, Stress.ULTIMATE -> Case.byVowel(glottalizeVowel(vcVk))
                ?: return Error("Unknown Vf form $vcVk (ultimate stress)")

            Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress in concatenated formative")
            else -> return Error("Stress error")
        }
    } else {
        if (isVerbal) {
            parseVk(vcVk) ?: return Error("Unknown Vk form $vcVk")
        } else {
            Case.byVowel(vcVk) ?: return Error("Unknown Vc form $vcVk")
        }
    }
    index++

    if (groups.lastIndex >= index) {
        val tail = groups.drop(index - 1).joinToString("")
        return Error("Formative continued unexpectedly: -$tail")
    }

    val slotList: List<Glossable> = listOfNotNull(concatenation, slotII, root, slotIV) +
        slotV + listOfNotNull(slotVI) + slotVIIAndMaybeSlotV + listOfNotNull(slotVIII, slotIX)

    return Parsed(slotList, stressMarked = relation)

}

private fun String.isInvalidRootForm(): Boolean = startsWith("h") || this in INVALID_ROOT_FORMS

private inline fun <reified R> Iterable<*>.findIsInstance(): R? {
    for (element in this) {
        if (element is R) return element
    }
    return null
}

fun parseModular(word: Word, marksMood: Boolean?): ParseOutcome {

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
            else -> return Error("Stress error: ${word.stress.name}")
        }

    }

    return Parsed(slot1, *midSlotList.toTypedArray(), slot5)

}

class Referential(private val referents: List<Slot>) : Glossable, List<Slot> by referents {
    override fun gloss(o: GlossOptions): String {
        return when (referents.size) {
            0 -> ""
            1 -> referents[0].gloss(o)
            else -> referents
                .joinToString(REFERENT_SEPARATOR, REFERENT_START, REFERENT_END)
                { it.gloss(o) }
        }
    }
}

fun parseReferential(word: Word): ParseOutcome {
    val essence = when (word.stress) {
        Stress.ULTIMATE -> Essence.REPRESENTATIVE
        Stress.MONOSYLLABIC, Stress.PENULTIMATE -> Essence.NORMAL
        Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress on referential")
        else -> return Error("Stress error")
    }

    if (word[0] in CP_CONSONANTS && word.size > 2) {
        return Error("Cp in referential not preceded by epenthetic \"äi\"")
    }

    if (word[0] == "äi" && word[1] !in CP_CONSONANTS) {
        return Error("Epenthetic \"äi\" not followed by a Cp form")
    }

    if (word[0] == "ë" && word[1] in CP_CONSONANTS) {
        return Error("Cp in referential not preceded by epenthetic \"äi\"")
    }

    val c1 = word
        .takeWhile { it !in setOf("w", "y") }
        .dropLast(1)
        .filter { it !in setOf("ë", "äi") }
        .takeIf { it.size <= 3 } ?: return Error("Too many (>3) initial consonant clusters")
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
            val caseB = Case.byVowel(vc2) ?: return Error("Unknown case: ${word[index]}")
            index++

            val c2 = word.getOrNull(index)
            val refB = if (c2 != null) {
                parseFullReferent(c2) ?: return Error("Unknown personal reference cluster: $c2")
            } else null

            index++
            if (word.getOrNull(index) == "ë") index++

            if (word.size > index) return Error("Referential is too long")

            return Parsed(refA, Shown(caseA), Shown(caseB), refB, stressMarked = essence)

        }

        word.size > index + 1 -> return Error("Referential is too long")

        else -> return Parsed(refA, caseA, stressMarked = essence)
    }
}

fun parseCombinationReferential(word: Word): ParseOutcome {
    val essence = when (word.stress) {
        Stress.ULTIMATE -> Essence.REPRESENTATIVE
        Stress.MONOSYLLABIC, Stress.PENULTIMATE -> Essence.NORMAL
        Stress.ANTEPENULTIMATE -> return Error("Antepenultimate stress on combination referential")
        else -> return Error("Stress error")
    }
    var index = 0

    if (word[0] in setOf("ë", "a")) {
        if ((word[0] == "a") != (word[1] in CP_CONSONANTS)) {
            return Error("Epenthetic ï must only be used with Suppletive forms")
        }
        index++
    }


    val ref = parseFullReferent(word[index]) ?: return Error("Unknown referent: ${word[index]}")

    index++

    val caseA = Case.byVowel(word[index]) ?: return Error("Unknown case: ${word[index]}")
    index++

    val specification = when (word[index]) {
        "x" -> Specification.BASIC
        "xt" -> Specification.CONTENTIAL
        "xp" -> Specification.CONSTITUTIVE
        "xx" -> Specification.OBJECTIVE
        else -> return Error("Unknown combination referential Specification: ${word[index]}")
    }
    index++

    val vxCsAffixes: MutableList<ValidAffix> = mutableListOf()
    while (true) {
        if (index + 1 > word.lastIndex) {
            break
        }

        val affix = Affix(word[index], word[index + 1]).parse().validate { return Error(it.message) }

        vxCsAffixes.add(affix)
        index += 2

    }

    val caseB = when (word.getOrNull(index)) {
        "a", null -> null
        "üa" -> Case.THEMATIC
        else -> Case.byVowel(word[index]) ?: return Error("Unknown case: ${word[index]}")
    }


    return Parsed(
        ref, Shown(caseA, condition = caseB != null), specification,
        *vxCsAffixes.toTypedArray(),
        caseB?.let { Shown(it) }, stressMarked = essence
    )

}

fun parseMultipleAffix(word: Word): ParseOutcome {
    val concatOnly = if (word.stress == Stress.ULTIMATE) {
        GlossString("{concatenated formative only}", "{concat.}")
    } else null
    var index = 0
    if (word[0] == "ë") index++

    val firstAffixVx = word[index + 1].removeSuffix("'")

    val czGlottal = firstAffixVx != word[index + 1]

    val firstAffix = Affix(cs = word[index], vx = firstAffixVx).parse().validate { return Error(it.message) }
    index += 2

    val cz = "${if (czGlottal) "'" else ""}${word[index]}"

    val scopeOfFirst = affixualAdjunctScope(cz) ?: return Error("Unknown Cz: ${word[index]}")
    index++

    val vxCsAffixes: MutableList<ValidAffix> = mutableListOf()

    while (true) {
        if (index + 1 > word.lastIndex) break

        val affix = Affix(word[index], word[index + 1]).parse().validate { return Error(it.message) }
        vxCsAffixes.add(affix)
        index += 2
    }

    if (vxCsAffixes.isEmpty()) return Error("Only one affix found in multiple affix adjunct")

    val vz = word.getOrNull(index)

    val scopeOfRest = if (vz != null) {
        affixualAdjunctScope(vz, isMultipleAdjunctVowel = true) ?: return Error("Unknown Vz: $vz")
    } else null

    return Parsed(firstAffix, scopeOfFirst, *vxCsAffixes.toTypedArray(), scopeOfRest, stressMarked = concatOnly)

}


fun parseAffixual(word: Word): ParseOutcome {
    val concatOnly = if (word.stress == Stress.ULTIMATE)
        GlossString("{concatenated formative only}", "{concat.}")
    else null

    if (word.size < 2) return Error("Affixual adjunct too short: ${word.size}")

    val affix = Affix(word[0], word[1]).parse().validate { return Error(it.message) }

    val vs = word.getOrNull(2)
    val scope = affixualAdjunctScope(vs) ?: return Error("Unknown Vs: $vs")

    return Parsed(affix, scope, stressMarked = concatOnly)

}

fun parseMoodCaseScopeAdjunct(word: Word): ParseOutcome {
    val value: Glossable = when (val v = word[1]) {
        "a" -> Mood.FACTUAL
        "e" -> Mood.SUBJUNCTIVE
        "i" -> Mood.ASSUMPTIVE
        "o" -> Mood.SPECULATIVE
        "ö" -> Mood.COUNTERFACTIVE
        "u" -> Mood.HYPOTHETICAL
        "ai" -> CaseScope.NATURAL
        "ei" -> CaseScope.ANTECEDENT
        "iu" -> CaseScope.SUBALTERN
        "oi" -> CaseScope.QUALIFIER
        "öi" -> CaseScope.PRECEDENT
        "ui" -> CaseScope.SUCCESSIVE
        else -> return Error("Unknown Mood/Case-Scope adjunct vowel: $v")
    }

    return Parsed(Shown(value))
}