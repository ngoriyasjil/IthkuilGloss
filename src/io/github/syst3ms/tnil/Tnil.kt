package io.github.syst3ms.tnil

fun wordTypeOf(groups: Array<String>) : WordType = when {
    groups.size == 1 && groups[0].isConsonant() ->  WordType.BIAS_ADJUNCT

    groups[0] in setOf("hl", "hm", "hn", "hr") && (groups.size == 2) -> WordType.SUPPLETIVE_ADJUNCT

    groups[0] == "h" && groups.size == 2 -> WordType.REGISTER_ADJUNCT

    (groups[0].isVowel() || groups[0] in setOf("w", "y"))
            && groups.all { it.isVowel() || it in CN_CONSONANTS } -> WordType.MODULAR_ADJUNCT

    groups.size >= 4 && groups[0] == "ë" && groups[3] in COMBINATION_PRA_SPECIFICATION
            || groups.size >= 3 && groups[0] !in CC_CONSONANTS && groups[2] in COMBINATION_PRA_SPECIFICATION
    -> WordType.COMBINATION_PRA

    groups.size in 2..3 && groups[1].isConsonant() && !groups[1].isModular()
            || groups.size in 4..5 && groups[1] == "y" && !groups[3].isModular() -> WordType.AFFIXUAL_ADJUNCT

    groups.size >= 5 && groups[0].isConsonant() && groups[2] in CZ_CONSONANTS
            || groups.size >= 6 && (groups[0] == "ë") && (groups[3] in CZ_CONSONANTS) -> WordType.AFFIXUAL_SCOPING_ADJUNCT

    (groups.last().isVowel() || groups.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.last()?.isVowel() == true )
            && groups.takeWhile { it !in setOf("w", "y") }.takeIf { it.isNotEmpty() }?.dropLast(1)?.all { it.isConsonant() || it == "ë" } == true
    -> WordType.PERSONAL_REFERENCE_ADJUNCT

    else -> WordType.FORMATIVE
}

fun parseWord(s: String, precision: Int, ignoreDefault: Boolean) : String {

    val nonIthkuil = s.defaultForm().filter { it.toString() !in ITHKUIL_CHARS }
    if (nonIthkuil.isNotEmpty()) {
        return error(
            "Non-ithkuil characters detected: " +
                    nonIthkuil.map { "\"$it\" (" + it.toInt().toString(16) + ")" }.joinToString() +
                    if (nonIthkuil.contains("[qˇ^ʰ]".toRegex())) " You might be writing in Ithkuil III. Try \"!gloss\" instead." else ""
        )
    }

    if ('-' in s) {
        return s.split('-').joinToString(CONCATENATION_SEPARATOR) { parseWord(it, precision, ignoreDefault) }
    }

    val stress = s.substituteAll(ALLOGRAPHS).splitGroups().findStress()

    val (groups, sentencePrefix) = stripSentencePrefix(s.defaultForm().splitGroups()) ?: return error("Empty word")

    val ssgloss = when (precision) {
        0 -> "[.]-"
        1 -> "[sentence:]-"
        2, 3, 4 -> "[sentence start]-"
        else -> ""
    }

    return (if (sentencePrefix) ssgloss else "") +  when (wordTypeOf(groups)) {
        WordType.BIAS_ADJUNCT -> Bias.byGroup(groups[0])?.toString(precision) ?: error("Unknown bias: ${groups[0]}")

        WordType.SUPPLETIVE_ADJUNCT -> {
            val v = groups[1]
            parseSuppletiveAdjuncts(groups[0], v, precision, ignoreDefault)
        }
        WordType.REGISTER_ADJUNCT -> {
            val (register, initial) = Register.byVowel(groups.last()) ?: return error("Unknown register adjunct: $s")
            return "<" + (if (initial) "" else "/") + register.toString(precision, ignoreDefault) + ">"
        }
        WordType.MODULAR_ADJUNCT -> parseModular(groups, precision, ignoreDefault, stress)

        WordType.COMBINATION_PRA -> parseCombinationPRA(groups, precision, ignoreDefault, stress)

        WordType.AFFIXUAL_ADJUNCT -> parseAffixual(groups, precision, ignoreDefault, stress)

        WordType.AFFIXUAL_SCOPING_ADJUNCT -> parseAffixualScoping(groups, precision, ignoreDefault, stress)

        WordType.PERSONAL_REFERENCE_ADJUNCT -> parsePRA(groups, precision, ignoreDefault, stress)

        WordType.FORMATIVE -> parseFormative(groups, precision, ignoreDefault, stress)
    }
}

fun parseFormative(groups: Array<String>, precision: Int, ignoreDefault: Boolean, stress: Int) : String {

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

    val vv = if (index == 0 && groups[0].isConsonant()) "a" else {
        groups[index].also { index++ }
    }.defaultForm()

    var rootMode = "root"

    val slotII = if (vv in SPECIAL_VV_VOWELS) {
        when (vv) {
            "ëi", "eë", "ëu", "öë" -> {
                rootMode = "affix"
                if (shortcut != null) return error("Shortcuts can't be used with a Cs-root")
            }
            "eä", "öä" -> rootMode = "reference"
        }
        parseSpecialVv(vv, shortcut) ?: return error("Unknown Vv value: $vv")
    } else parseVv(vv, shortcut) ?: return error("Unknown Vv value: $vv")

    val root = when (rootMode) {
        "root" -> {
            val stem = slotII.getStem() ?: return error("No stem found: $vv")
            val (rootGloss, stemUsed) = parseRoot(groups[index], precision, stem)
            if (stemUsed) slotII.stemUsed = true
            rootGloss
        }
        "affix" -> {
            val vx = bySeriesAndForm(1, seriesAndForm(groups[index+1]).second) ?: return error("Unknown Cs-root Vr value: ${groups[index+1]}")
            parseAffix(groups[index], vx, precision, ignoreDefault, noType = true)
        }
        "reference" -> {
            (parseFullReferent(groups[index], precision, ignoreDefault) ?: "**${groups[index]}**")
        }
        else -> return error("Unable to parse root: ${groups[index]}, $rootMode")
    }
    index++



    val vr = if (shortcut != null) "a" else {
        groups.getOrNull(index).also { index++ } ?: return error("Formative ended unexpectedly: ${groups.joinToString("")}")
    }

    val slotIV = when (rootMode) {
        "root", "reference" -> parseVr(vr) ?: return error("Unknown Vr value: $vr")
        "affix" -> parseAffixVr(vr) ?: return error("Unknown Cs-root Vr value: $vr")
        else -> return error("A bug has occured: Unknown rootmode: $rootMode")
    }

    val csVxAffixes: MutableList<Affix> = mutableListOf()

    if (shortcut == null) {
        var indexV = index
        while (true) {
            if (indexV + 1 > groups.lastIndex || groups[indexV] in CN_CONSONANTS || groups[indexV] == "-") {
                csVxAffixes.clear()
                indexV = index
                break
            } else if (groups[indexV].isGlottalCa()) {
                break
            }

            val (vx, glottal) = unGlottalVowel(groups[indexV+1]) ?: return error("Unknown vowelform: ${groups[indexV+1]} (slot V)")

            csVxAffixes.add(Affix(vx, groups[index]))
            indexV += 2

            if (glottal && (groups.lastIndex >= indexV)) break
        }
        index = indexV

    }

    if (csVxAffixes.size == 1) csVxAffixes[0].canBePraShortcut = true


    var cnInVI = false

    val slotVI = if (shortcut == null) {
        val ca = if (groups.getOrNull(index)?.isGlottalCa()
                        ?: return error("Formative ended before Ca")) {
            if (csVxAffixes.isNotEmpty()) {
                groups[index].unGlottalCa()
            } else return error("Unexpected glottal Ca: ${groups[index]}")
        } else groups[index]

        if (ca !in setOf("hl", "hr", "hm", "hn", "hň")) {
            parseCa(ca).also { index++ } ?: return error("Unknown Ca value: $ca")
        } else {
            parseCa("l")!!.also{ cnInVI = true }
        }
    } else null

    val vxCsAffixes : MutableList<Precision> = mutableListOf()

    if (!cnInVI) {
        while (true) {
            if (index+1 >= groups.size || groups[index+1] in CN_CONSONANTS || groups[index+1] == "-") {
                break
            }

            val (vx, glottalVowel) = unGlottalVowel(groups[index]) ?: return error("Unknown vowelform: ${groups[index]} (slot VII)")

            val glottalCs = groups[index+1].startsWith("'")

            vxCsAffixes.add(Affix(vx, groups[index+1].removePrefix("'")))
            index += 2

            if (glottalVowel || glottalCs) {
                vxCsAffixes.add(PrecisionString("{end of slot V}", "{Ca}"))
            }
        }
    }

    if (vxCsAffixes.size == 1) (vxCsAffixes[0] as? Affix)?.canBePraShortcut = true

    val marksMood = (stress == 0) || (stress == -1)

    val slotVIII: Slot? = when {
        cnInVI -> {
            parseVnCn("a", groups[index], marksMood).also { index++ } ?: return error("Unknown Cn value in Ca: ${groups[index]}")
        }
        groups.getOrNull(index+1) in CN_CONSONANTS -> {
            parseVnCn(groups[index], groups[index+1], marksMood).also { index += 2 } ?: return error("Unknown VnCn value: ${groups[index] + groups[index+1]}")
        }
        else -> null
    }


    val vcVk = groups.getOrNull(index) ?: "a"

    val slotIX : Precision = if (concatenation == null) {
        when (stress) {
            0, -1 -> parseVk(vcVk) ?: return error("Unknown Vk form $vcVk")
            1, 2 -> Case.byVowel(vcVk) ?: return error("Unknown Vc form $vcVk")
            else -> return error("Unknown stress: $stress from ultimate")
        }
    } else {
        when (stress) {
            1 -> Case.byVowel(vcVk) ?: return error("Unknown Vf form $vcVk (penultimate stress)")
            0 -> {
                val glottalified = when (vcVk.length) {
                    1 -> "$vcVk'$vcVk"
                    2 -> "${vcVk[0]}'${vcVk[1]}"
                    else -> return error("Vf form is too long: $vcVk")
                }
                Case.byVowel(glottalified) ?: return error("Unknown Vf form $vcVk (ultimate stress)")
            }
            else -> return error("Unknown stress for concatenated formative: $stress from ultimate")
        }
    }
    index++

    val cyMarksMood = (stress == 0) || (stress == -1) || (stress == 2 && slotVIII != null)

    val slotX = if (concatenation == null && index < groups.size) {
        parseCbCy(groups[index], cyMarksMood)
    } else null

    val slotList: List<Precision> = listOfNotNull(relation, concatenation, slotII, PrecisionString(root), slotIV) +
            csVxAffixes + listOfNotNull(slotVI) + vxCsAffixes + listOfNotNull(slotVIII, slotIX, slotX)

    return slotList.glossSlots(precision, ignoreDefault)

}

fun parseAffixVr(vr: String): Slot? {
    val (series, form) = seriesAndForm(vr)
    if (form !in 1..9) return null

    val degree = PrecisionString("degree $form", "D$form")

    val specification = when (series) {
        1 -> Specification.BASIC
        2 -> Specification.CONTENTIAL
        3 -> Specification.CONSTITUTIVE
        4 -> Specification.OBJECTIVE
        else -> return null
    }

    return Slot(degree, specification)
}

@Suppress("UNCHECKED_CAST")
fun parseModular(groups: Array<String>, precision: Int, ignoreDefault: Boolean, stress: Int) : String {

    var index = 0

    val slot1 = when (groups[0]) {
        "w" -> PrecisionString("{parent formative only}", "{parent}")
        "y" -> PrecisionString("{concatenated formative only}", "{concat.}")
        else -> null
    }

    if (slot1 != null) index++

    val midSlotList : MutableList<Slot> = mutableListOf()

    while (groups.size > index + 2) {
        midSlotList.add(parseVnCn(groups[index], groups[index+1], false) ?: return error("Unknown VnCn: ${groups[index]}${groups[index+1]}"))
        index += 2
    }

    if (midSlotList.size > 3) return error("Too many (>3) middle slots in modular adjunct: ${midSlotList.size}")

    val slot5 = when {
        midSlotList.isEmpty() -> Aspect.byVowel(groups[index]) ?: return error("Unknown aspect: ${groups[index]}")
        stress == 1 -> parseVnCn(groups[index], "h", marksMood = true) ?: return error("Unknown non-aspect Vn: ${groups[index]}")
        stress == 0 -> parseVh(groups[index]) ?: return error("Unknown Vh: ${groups[index]}")
        else -> return error("Unknown stress on modular adjunct: $stress from ultimate")
    }

    return listOfNotNull(slot1, *midSlotList.toTypedArray(), slot5).glossSlots(precision, ignoreDefault)

}

fun parsePRA(groups: Array<String>, precision: Int, ignoreDefault: Boolean, stress: Int) : String {
    val essence = (if (stress == 0) Essence.REPRESENTATIVE else Essence.NORMAL).toString(precision, ignoreDefault)
    val c1 = groups
            .takeWhile { it !in setOf("w", "y") }
            .filter { it != "ë"}
            .dropLast(1)
            .takeIf { it.size <= 6 }
            ?.joinToString("") ?: return error("Too many (>3) initial ë-separated consonants")
    val refA = parseFullReferent(c1, precision, ignoreDefault) ?: return error("Unknown personal reference cluster: $c1")
    var index = (groups
            .indexOfFirst { it in setOf("w", "y") }
            .takeIf { it != -1 } ?: groups.size) - 1

    val caseA = Case.byVowel(groups[index])?.toString(precision, ignoreDefault) ?: return error("Unknown case: ${groups[index]}")
    index++

    when {
        groups.getOrNull(index) in setOf("w", "y") -> {
            index++
            val vc2 = groups.getOrNull(index) ?: return "PRA ended unexpectedly"
            val caseB = Case.byVowel(vc2)?.toString(precision, ignoreDefault) ?: return error("Unknown case: ${groups[index]}")
            index++

            val c2 = groups.getOrNull(index)
            val refB = if (c2 != null) {
                parseFullReferent(c2, precision, ignoreDefault) ?: return error("Unknown personal reference cluster: $c2")
            } else null

            index++
            if (groups.getOrNull(index) == "ë") index++

            if (groups.size > index) return error("PRA is too long")

            return listOfNotNull(refA, caseA, caseB, refB, essence).filter { it.isNotEmpty() }.joinToString(SLOT_SEPARATOR)

        }
        groups.size > index+1 -> return error("PRA is too long")

        else -> return listOfNotNull(refA, caseA, essence).filter { it.isNotEmpty() }.joinToString(SLOT_SEPARATOR)
    }
}

@Suppress("UNCHECKED_CAST")
fun parseCombinationPRA(groups: Array<String>,
                        precision: Int,
                        ignoreDefault: Boolean,
                        stress: Int): String {
    val essence = if (stress == 0) Essence.REPRESENTATIVE else Essence.NORMAL
    var index = 0

    if (groups[0] == "ë") index++

    val ref = PrecisionString(parseFullReferent(groups[index], precision, ignoreDefault) ?: return error("Unknown referent: ${groups[index]}"))
    index++

    val caseA = Case.byVowel(groups[index]) ?: "Unknown case: ${groups[index]}"
    index++

    val specification = when(groups[index]) {
        "x" -> Specification.BASIC
        "xx" -> Specification.CONTENTIAL
        "lx" -> Specification.CONSTITUTIVE
        "rx" -> Specification.OBJECTIVE
        else -> return error("Unknown combination PRA specification: ${groups[index]}")
    }
    index++

    val vxCsAffixes : MutableList<Precision> = mutableListOf()
    while (true) {
        if (index+1 >= groups.size || groups[index+1] in CN_CONSONANTS || groups[index+1] == "-") {
            break
        }

        val (vx, glottal) = unGlottalVowel(groups[index]) ?: return error("Unknown vowelform: ${groups[index]} (slot VII)")

        if (glottal) return "Unexpected glottal stop"

        vxCsAffixes.add(Affix(vx, groups[index+1]))
        index += 2

    }

    val caseB = when (groups.getOrNull(index)?.defaultForm()) {
        "a", null -> null
        "üa" -> Case.THEMATIC
        else -> Case.byVowel(groups[index]) ?: return error("Unknown case: ${groups[index]}")
    }

    val slotList = listOfNotNull(ref, caseA, specification, *vxCsAffixes.toTypedArray(), caseB, essence)

    return slotList.map {
        if (it is List<*>) {
            (it as List<Precision>).glossSlots(precision, ignoreDefault) // Wacky casting, beware.
        } else (it as Precision).toString(precision, ignoreDefault) }
            .filter { it.isNotEmpty() }
            .joinToString(SLOT_SEPARATOR)

}

fun parseAffixualScoping(groups: Array<String>,
                         precision: Int,
                         ignoreDefault: Boolean,
                         stress: Int): String {
    val concatOnly = if (stress == 0) PrecisionString("{concatenated formative only}","{concat.}") else null
    var index = 0
    if (groups[0] == "ë") index++
    val firstAffix = Affix(groups[index+1], groups[index])
    index += 2
    val scopeOfFirst = affixAdjunctScope(groups[index], ignoreDefault) ?: return error("Unknown Cz: ${groups[index]}")
    index++

    val vxCsAffixes : MutableList<Precision> = mutableListOf()

    while (true) {
        if (index+1 > groups.lastIndex) break

        val (vx, glottal) = unGlottalVowel(groups[index]) ?: return error("Unknown vowelform: ${groups[index]}")

       if (glottal) return error("Unexpected glottal stop in affixual scoping adjunct")

        vxCsAffixes.add(Affix(vx, groups[index+1]))
        index += 2
    }

    if (vxCsAffixes.isEmpty()) return error("Only one affix found in affixual scoping adjunct")

    val vz = groups.getOrNull(index)

    val scopeOfRest = if (vz != null) {
        affixAdjunctScope(vz, scopingAdjunctVowel = true) ?: return error("Unknown Vz: $vz")
    } else null

    return listOfNotNull(firstAffix, scopeOfFirst, *vxCsAffixes.toTypedArray(), scopeOfRest, concatOnly)
        .glossSlots(precision, ignoreDefault)

}


fun parseAffixual(groups: Array<String>,
                  precision: Int,
                  ignoreDefault: Boolean,
                  stress: Int) : String {
    val concatOnly = if (stress == 0) PrecisionString("{concatenated formative only}", "{concat.}") else null

    if (groups.size < 2) return error("Affixual adjunct too short: ${groups.size}")

    val affix = Affix(groups[0], groups[1])
    val scope = affixAdjunctScope(groups.getOrNull(2), ignoreDefault)

    return listOfNotNull(affix, scope, concatOnly).glossSlots(precision, ignoreDefault)

}


