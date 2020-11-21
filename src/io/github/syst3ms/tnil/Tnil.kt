@file:Suppress("IMPLICIT_CAST_TO_ANY")

package io.github.syst3ms.tnil

import java.io.PrintWriter
import java.io.StringWriter

fun main() {
    println(parseWord("lada'lad", 1, true))
}

fun parseSentence(s: String, precision: Int, ignoreDefault: Boolean): List<String> {
    if (s.isBlank()) {
        return errorList("Nothing to parse.")
    }
    val words = s.toLowerCase().split("\\s+".toRegex())
    val state = SentenceParsingState()
    var currentlyCarrier = false
    var modularIndex : Int? = null
    var modularForcedStress : Int? = null
    val result = arrayListOf<String>()
    for ((i, word) in words.withIndex()) {
        var toParse = word
        if (!currentlyCarrier || !state.carrier) {
            if (toParse.startsWith(LOW_TONE_MARKER)) { // Register end
                toParse = toParse.drop(1)
            } else if (toParse matches "'[aeoui]'".toRegex()) {
                state.forcedStress = when (toParse[1]) {
                    'a' -> -1
                    'e' -> 0
                    'o' -> 1
                    'u' -> 2
                    'i' -> 3
                    else -> throw IllegalStateException()
                }
                continue
            }
        }
        if (toParse.any { !it.toString().isConsonant() && !it.toString().isVowel() }) {
            return errorList("**Parsing error**: '$word' contains non-Ithkuil characters")
        }
        try {
            val res = parseWord(toParse, precision, ignoreDefault)
            if (currentlyCarrier && state.carrier) {
                result += if (word.startsWith(LOW_TONE_MARKER)) {
                    currentlyCarrier = false
                    state.carrier = false
                    "${word.drop(1)} $CARRIER_END "
                } else {
                    "$word "
                }
                continue
            } else if (res == MODULAR_PLACEHOLDER) { // Modular adjunct
                modularIndex = i
                modularForcedStress = state.forcedStress
                continue
            } else if (res.startsWith("\u0000")) {
                return errorList("**Parsing error**: ${res.drop(1)}")
            } else if (word.startsWith(LOW_TONE_MARKER)) {
                if (state.register.isEmpty())
                    return errorList("*Syntax error*: low tone can't mark the end of non-default register, since no such register is active.")
                val reg = state.register.removeAt(state.register.lastIndex)
                result += if (reg == Register.DISCURSIVE) {
                    "$res $DISCURSIVE_END "
                } else {
                    "$res $REGISTER_END "
                }
                continue
            } else if ((state.isLastFormativeVerbal != null || state.quotativeAdjunct) && modularIndex != null) {
                // Now we can know the stress of the formative and finally parse the adjunct properly
                val mod = parseModular(
                        words[modularIndex].splitGroups(),
                        precision,
                        // If quotativeAdjunct is true, case-scope needs default values like CTX or MNO to be shown, and we want to ignore them
                        state.quotativeAdjunct || ignoreDefault,
                        // This is fine because if quotativeAdjunct is false that means isLastFormativeVerbal is non-null
                        !state.quotativeAdjunct && state.isLastFormativeVerbal!!,
                        modularForcedStress,
                        sentenceParsingState = state
                )
                if (mod.startsWith("\u0000")) {
                    return errorList("**Parsing error**: ${mod.drop(1)}")
                }
                result.add(modularIndex, "$mod ")
                state.quotativeAdjunct = false
                modularIndex = null
                modularForcedStress = null
            }
            currentlyCarrier = state.carrier
            result += res + if (state.carrier && !res.endsWith(CARRIER_START)) {
                " $CARRIER_START"
            } else {
                " "
            }
        } catch (e: Exception) {
            logger.error("{}", e)
            return if (precision < 3) {
                errorList("A severe exception occurred during sentence parsing. We are unable to give more information. " +
                        "For a more thorough (but technical) description of the error, please use debug mode.")
            } else {
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                val stacktrace = sw.toString()
                        .split("\n")
                        .take(10)
                        .joinToString("\n")
                errorList(stacktrace)
            }
        }
    }
    if (modularIndex != null && modularForcedStress != null) {
        return errorList("A modular adjunct needs an adjacent formative. If you want to parse the adjunct by itself, use ??gloss, ??short or ??full.")
    }
    if (currentlyCarrier && state.carrier) {
        result += "$CARRIER_END "
    }
    for (reg in state.register.asReversed()) {
        result += if (reg == Register.DISCURSIVE) {
            "$DISCURSIVE_END "
        } else {
            "$REGISTER_END "
        }
    }
    if (state.concatenative) {
        result += "$CONCATENATIVE_END "
    }
    return result
}

fun parseWord(s: String, precision: Int, ignoreDefault: Boolean) : String {
    val groups = s.splitGroups()
    if (groups.isEmpty())
        return error("Empty word")
    return when {
        groups.size == 1 && groups[0].isConsonant() ->  {
            Bias.byGroup(groups[0])?.toString(precision) ?: error("Unknown bias: ${groups[0]}")
        }
        groups[0] in setOf("ç", "hl", "hr", "hm") && (groups.size == 2 || groups.size == 4 && groups[2] == "'") -> {
            val v = if (groups.size == 4) groups[1] + groups[2] + groups[3] else groups[1]
            parseCarrierAdjuncts(groups[0], v, precision, ignoreDefault) ?: error("Unknown carrier adjunct: $s")
        }
        groups[0] == "h" && groups.size == 2 -> {
            val (register, initial) = Register.byVowel(groups.last()) ?: return error("Unknown register adjunct: $s")
            return "<" + (if (initial) "" else "/") + register.toString(precision, ignoreDefault) + ">"
        }
        groups.size == 2 && groups[0].isConsonant() && !groups[0].isModular()
                || groups.size >= 4 && !groups[0].isModular() && (groups[1] == "ë" || groups[2] matches "[wy]".toRegex() || groups[2] == "'" && (groups.size == 4 || groups[4] matches "[wy]".toRegex())) -> {
            parsePRA(groups, precision, ignoreDefault)
        }
        groups.size >= 4 && groups[0].isVowel() && groups[3] in COMBINATION_PRA_SPECIFICATION
                || groups.size >= 3 && groups[0] !in CD_CONSONANTS && groups[2] in COMBINATION_PRA_SPECIFICATION
                || groups.size >= 6 && groups[0].isVowel() && groups[3] == "'" && groups[5] in COMBINATION_PRA_SPECIFICATION
                || groups.size >= 5 && groups[0] !in CD_CONSONANTS && groups[2] == "'" && groups[4] in COMBINATION_PRA_SPECIFICATION -> {
            parseCombinationPRA(groups, precision, ignoreDefault)
        }
        groups.size in 2..3 && groups[1].isConsonant() && !groups[1].isModular()
                || groups.size in 4..5 && groups[1] == "y" && !groups[3].isModular() -> {
            parseAffixual(groups, precision, ignoreDefault)
        }
        groups.all { it.isVowel() || it.isModular() } -> {
            parseModular(groups, precision, ignoreDefault)
        }
        else -> parseFormative(groups, precision, ignoreDefault)
    }
}

fun parseFormative(groups: Array<String>,
                   precision: Int,
                   ignoreDefault: Boolean,
                   sentenceParsingState: SentenceParsingState? = null): String {
    val stress = (sentenceParsingState?.forcedStress ?: groups.findStress()).coerceAtLeast(0)
    var firstSegment = Relation.values()[stress / 2].toString(precision, ignoreDefault).plusSeparator()
    var i = 0
    var rtiScope = sentenceParsingState?.rtiAffixScope
    var referentParsingData: PersonalReferentParsingData? = null
    // First we need to determine if the formative is short, simple or complex
    if (groups[0] in CD_CONSONANTS) { // Complex formative
        if (groups.size < 7) { // Minimum possible for a complex formative
            return error("Complex formative ended unexpectedly: ${groups.joinToString("")}")
        }
        var stackedPerspectiveIndex: Int? = null //Slot III Ca-stacking for use with the personal reference roots
        val (cd, altVf, slotThreePresent) = parseCd(groups[0]) ?: return error("Unknown Cd value: ${groups[0]}")
        val vf = Case.byVowel(groups[1], vfShort = altVf)
                ?: return error("Unknown case value: ${groups[1]}")
        firstSegment += cd.toString(precision, ignoreDefault).plusSeparator()
        firstSegment += vf.toString(precision).plusSeparator()
        i = 2 // Slot III begins at index 2
        if (slotThreePresent) {
            val limit = groups.size - 5 // Conservative upper bound
            var stop = false
            while (i < limit && !stop) {
                val c = groups[i]
                var v: String
                if (i + 3 < limit && groups[i+2] == "'") {
                    v = if (groups[i+1] eq groups[i+3]) {
                        groups[i+1]
                    } else {
                        groups[i+1] + groups[i+3]
                    }
                    stop = true
                    i += 4
                } else {
                    v = groups[i+1]
                    stop = groups[i+2].startsWith("'") || groups[i+1] == CA_STACKING_VOWEL && groups[i+2].isGlottalCa() //the latter part is almost certainly illegal
                    i += 2
                }
                if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
                     return error("'$c' can't be a valid affix consonant")
                val aff = parseAffix(c, v, precision, ignoreDefault, slotThree = true)
                when {
                    aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                    aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                    aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
                    aff.contains(AFFIX_STACKED_CA_MARKER) -> { // We handle the case of personal referents if the affix is Ca-stacking
                        if (stackedPerspectiveIndex == null) {
                            stackedPerspectiveIndex = aff.substringBefore(AFFIX_STACKED_CA_MARKER).toInt()
                        }
                        firstSegment += aff.substringAfter(AFFIX_STACKED_CA_MARKER).plusSeparator()
                    }
                    else -> firstSegment += aff.plusSeparator()
                }
            }
        }
        // Complex Vv and Vr have the exact same values
        val complexVv = parseComplexVv(groups[i+1]) ?: return error("Unknown complex Vv value: ${groups[i+1]}")
        var stem = (complexVv.first { it is Stem } as Enum<*>).ordinal
        val c = if (groups[i].startsWith("'") && groups[i].length > 1 && slotThreePresent) {
            groups[i].trimGlottal()
        } else {
            groups[i]
        }
        if (c.isInvalidLexical()) // We don't want to error if it's just slot III marking
            return error("'$c' can't be a valid root consonant")
        val (ci, ciStemUsed) = parseRoot(c, precision, stem)
        if (sentenceParsingState?.carrier == false && c == "s")
            sentenceParsingState.carrier = true
        if (precision > 0 && stem != 0 && stackedPerspectiveIndex != null && (c == "n" || c == "d")) {
            if (c == "n") {
                val desc = animateReferentDescriptions[stem - 1][stackedPerspectiveIndex]
                // replaceFirst because there might be multiple stacked Ca
                firstSegment = firstSegment.replaceFirst("\\b[MPNA]\\b".toRegex(), "__$0__")
                firstSegment += "'$desc'".plusSeparator()
            } else {
                val desc = inanimateReferentDescriptions[stem - 1][stackedPerspectiveIndex]
                firstSegment = firstSegment.replaceFirst("\\b[MPNA]\\b".toRegex(), "__$0__")
                firstSegment += "'$desc'".plusSeparator()
            }
        } else {
            firstSegment += ci.plusSeparator()
        }
        val complexVr = parseComplexVr(groups[i+3], groups[i+4].isGlottalCa()) ?: return error("Unknown complex Vr value: ${groups[i+3]}")
        stem = (complexVr.first { it is Stem } as Enum<*>).ordinal
        if (groups[i+2].isInvalidLexical())
            return error("'${groups[i+2]}' can't be a valid root consonant")
        val (cr, crStemUsed) = parseRoot(groups[i+2], precision, stem)
        if (sentenceParsingState?.carrier == false && groups[i+2] == "s")
            sentenceParsingState.carrier = true
        firstSegment += complexVv.toString(precision, ignoreDefault, stemUsed = ciStemUsed).plusSeparator()
        firstSegment += (if (precision > 0 && stem != 0 && groups[i+2] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else if (precision > 0 && stem != 0 && groups[i+2] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else {
            cr
        }).plusSeparator()
        firstSegment += complexVr.toString(precision, ignoreDefault, stemUsed = crStemUsed).plusSeparator()
        i += 4
    } else { // Simple formative
        val newGroups = if (groups[0].isConsonant()) {
            arrayOf("a") + groups
        } else groups // initial "a"-elision

        if (newGroups.size < 4) {
            return error("Simple formative ended unexpectedly: ${groups.joinToString("")}")
        }
        val vvParse = if (newGroups[1] matches "[wy]".toRegex()) {
            i += 2
            newGroups[0] + newGroups[1] + newGroups[2]
        } else {
            newGroups[0]
        }
        val (vv, negShortcut) = parseSimpleVv(vvParse) ?: return error("Unknown Vv value: $vvParse")
        val vr = parseSimpleVr(newGroups[i+2]) ?: return error("Unknown Vr value: ${newGroups[i+2]}")
        val stem = (vv[0] as Stem).ordinal
        if (newGroups[i+1].isInvalidLexical())
            return error("'${newGroups[i+1]}' can't be a valid root consonant")
        val (cr, stemUsed) = parseRoot(newGroups[i+1], precision, stem)
        if (sentenceParsingState?.carrier == false && newGroups[i+1] == "s") {
            sentenceParsingState.carrier = true
        }
        firstSegment += join(
                vv.toString(precision, ignoreDefault, stemUsed = stemUsed),
                if (negShortcut) parseAffix("r", "ë", precision, ignoreDefault) else "")
                .plusSeparator()
        firstSegment += (if (precision > 0 && stem != 0 && newGroups[i+1] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else if (precision > 0 && stem != 0 && newGroups[i+1] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else {
            cr
        }).plusSeparator()
        firstSegment += vr.toString(precision, ignoreDefault).plusSeparator()
        i += 3

        if (groups[0].isConsonant()) i-- //a-elision
    }

    // i is now either at Ca or the beginning of Slot VIII
    var secondSegment = ""
    var j = groups.lastIndex
    // Start from the end to easily identify each slot
    val noGlottalTail = j >= 7
            && groups[j-1].isVowel()
            && (groups[j-2].isModular() || groups[j-2] == "'" && groups[j-4].isModular())
    val glottalTail = j >= 6 && groups[j].startsWith("'")
    if (noGlottalTail || glottalTail) { // Bias
        val c = groups[j].trimGlottal()
        val bias = Bias.byGroup(c)
        val alternate = if (stress == 0 || stress == 3) {
            Mood.byCy(c)
        } else {
            CaseScope.byCy(c)
        }
        secondSegment += (bias?.toString(precision)
                ?: alternate?.toString(precision)
                ?: return error("Unknown bias/case-scope/mood: $c")).plusSeparator(start = true)
        j--
    }
    if (groups[j].isVowel()) { // Vc/Vk
        val v = if (groups[j-1] == "'" && stress > 0) {
            j -= 2
            groups[j] + "'" + groups[j+2]
        } else if (groups[j-1] == "'") {
            j -= 2
            when {
                // We've already established stress, it doesn't really matter which one of the two bears it
                groups[j] eq groups[j+2] -> groups[j]
                else -> groups[j] + groups[j+2]
            }
        } else {
            groups[j]
        }
        val vcvk = if (stress == 0) {
            parseVk(v)?.toString(precision, ignoreDefault)
                    ?: return error("Unknown illocution/expectation/validation: $v")
        } else {
            Case.byVowel(v)?.toString(precision, ignoreDefault) ?: return error("Unknown case vowel: $v")
        }.plusSeparator(start = true)
        secondSegment = "$vcvk$secondSegment"
        j--
    }
    if (j - i >= 2 && groups[j] matches "('?[hw].*|'y)".toRegex()) { // Cn
        when {
            groups[j].startsWith("h") -> {
                val cn = if (stress == 0) {
                    Mood.byCn(groups[j])
                } else {
                    CaseScope.byCn(groups[j])
                } ?: return error("Unknown case-scope/mood: ${groups[j]}")
                val vn = when {
                    groups[j-2] == "y" -> {
                        j -= 2
                        groups[j-1] + "y" + groups[j+1]
                    }
                    else -> groups[j-1]
                }
                val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                        ?: return error("Unknown valence/phase/level/effect: $vn")
                secondSegment = join(patternOne, cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
            }
            groups[j].startsWith("'h") -> {
                val cnString = groups[j].trimGlottal()
                val cn = if (stress == 0) {
                    Mood.byCn(cnString)
                } else {
                    CaseScope.byCn(cnString)
                } ?: return error("Unknown case-scope/mood: $cnString")
                val vt = Aspect.byVowel(groups[j-1]) ?: return error("Unknown aspect: ${groups[j-1]}")
                secondSegment = join(vt.toString(precision, ignoreDefault), cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
            }
            else -> {
                assert(groups[j] matches "'?[wy]".toRegex())
                val fncCn = if (groups[j-1].defaultForm().endsWith("u")) {
                    "y"
                } else {
                    "w"
                }
                val contextIndex = listOf(fncCn, "'w", "'y").indexOf(groups[j])
                if (contextIndex == -1)
                    return error("Expected the Cn value to be $fncCn, but found '${groups[j]}'")
                val context = Context.values()[contextIndex + 1]
                val vn = when {
                    groups[j-2] == "y" -> {
                        j -= 2
                        groups[j-1] + "y" + groups[j+1]
                    }
                    else -> groups[j-1]
                }
                val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                        ?: return error("Unknown phase/context: $vn")
                secondSegment = join(patternOne, context.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
            }
        }
        j -= 2
    }
    if (secondSegment.isEmpty() && stress == 0) { // Ensure that ASR/COG/OBS be always marked
        secondSegment = "ASR/COG/OBS".plusSeparator(start = true)
    }
    var specialAffixJoin = false
    // j is now either at Ca, or at the end of Slot X
    if (i == j) { // We're at Ca, slots VIII and X are empty
        val c = groups[i].trimGlottal()
        val ca = parseCa(c)
        val alternate = if (c != "h" && c.startsWith("h")) {
            if (stress == 0) {
                Mood.byCn(c)?.toString(precision)
            } else {
                CaseScope.byCn(c)?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (ca != null) {
            if (referentParsingData?.isInanimate == false) {
                val desc = animateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca)]
                firstSegment = firstSegment.replace(REFERENT_ROOT_PLACEHOLDER, "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            } else if (referentParsingData?.isInanimate == true) {
                val desc = inanimateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca)]
                firstSegment = firstSegment.replace(REFERENT_ROOT_PLACEHOLDER, "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            }
        }
        sentenceParsingState?.rtiAffixScope = null
        sentenceParsingState?.isLastFormativeVerbal = stress == 0
        return firstSegment.dropLast(SLOT_SEPARATOR.length) +
            (caString
                    ?: alternate
                    ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood: ${groups[i]}")).plusSeparator(start = true) +
            secondSegment
    } else if (groups[j].isGlottalCa() || groups[j-2] == "'") { // We're at Ca, slot X is empty, but slot VIII isn't
        val c = groups[j].trimGlottal()
        val ca = parseCa(c)
        val alternate = if (c != "h" && c.startsWith("h")) {
            if (stress == 0) {
                Mood.byCn(c)?.toString(precision)
            } else {
                CaseScope.byCn(c)?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (ca != null) {
            if (referentParsingData?.isInanimate == false) {
                val desc = animateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca)]
                firstSegment = firstSegment.replace(REFERENT_ROOT_PLACEHOLDER, "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            } else if (referentParsingData?.isInanimate == true) {
                val desc = inanimateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca)]
                firstSegment = firstSegment.replace(REFERENT_ROOT_PLACEHOLDER, "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            }
        }
        secondSegment = (caString ?: (alternate
                ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood: $c"))) + secondSegment
        j--
    } else {
        var caIndex = i
        for (k in j downTo i) {
            if (groups[k] == "'" || groups[k] == "'y") { // End of slot VIII, but the glottal stop isn't part of Ca
                caIndex = k + 2
                break
            } else if (groups[k].isGlottalCa()) { // Ca reached
                caIndex = k
                break
            }
        }
        var potentialPraShortcut : Pair<String, String>? = null
        var isAlone : Boolean? = null
        while (j > caIndex) {
            isAlone = isAlone == null
            if (!isAlone && potentialPraShortcut != null) {
                val a = parseAffix(potentialPraShortcut.first, potentialPraShortcut.second, precision, ignoreDefault)
                when {
                    a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${a.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                    a.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${a.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                    a.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${a.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
                }
                if (potentialPraShortcut.first == RTI_AFFIX_CONSONANT)
                    rtiScope = rtiScope ?: "{Ca}"
                secondSegment = a.plusSeparator(start = true) + secondSegment
            }
            if (j - 1 <= caIndex) {
                return error("Affix group (slot X) ended unexpectedly")
            }
            val c = groups[j]
            val v = if (groups[j-2] == "y") {
                j -= 2
                groups[j-1] + "y" + groups[j+1]
            } else {
                groups[j-1]
            }
            if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
                return error("'$c' can't be a valid affix consonant")
            val a = parseAffix(c, v, precision, ignoreDefault, canBePraShortcut = isAlone)
            when {
                a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${a.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${a.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${a.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
                a == PRA_SHORTCUT_AFFIX_MARKER && potentialPraShortcut == null -> {
                    potentialPraShortcut = c to v
                    j -= 2
                    continue
                }
            }
            secondSegment = a.plusSeparator(start = true) + secondSegment
            if (c == RTI_AFFIX_CONSONANT)
                rtiScope = rtiScope ?: "{Ca}"
            j -= 2
        }
        if (potentialPraShortcut != null && isAlone == true) { // PRA shortcut
            val shortcut = parsePraShortcut(potentialPraShortcut.first, potentialPraShortcut.second, precision)
                    ?: return error("Unknown personal referent: '" + potentialPraShortcut.first + "'")
            secondSegment = shortcut.plusSeparator(start = true) + secondSegment.replace(PRA_SHORTCUT_AFFIX_MARKER, shortcut)
        }
        val c = groups[caIndex].trimGlottal()
        val ca = parseCa(c)
        val alternate = if (c != "h" && c.startsWith("h")) {
            if (stress == 0) {
                Mood.byCn(c)?.toString(precision)
            } else {
                CaseScope.byCn(c)?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (ca != null) {
            if (referentParsingData?.isInanimate == false) {
                val desc = animateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca)]
                firstSegment = firstSegment.replace(REFERENT_ROOT_PLACEHOLDER, "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            } else if (referentParsingData?.isInanimate == true) {
                val desc = inanimateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca)]
                firstSegment = firstSegment.replace(REFERENT_ROOT_PLACEHOLDER, "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            }
        }
        secondSegment = (caString ?: (alternate
                ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood: $c"))) + secondSegment
        specialAffixJoin = isAlone != null && caString.isNullOrEmpty()
        j = caIndex - 1
    }
    // j is now at the vowel before Ca
    var potentialPraShortcut : Pair<String, String>? = null
    var isAlone : Boolean? = null
    var k = i
    while (k <= j) { // Reminder : affixes are CV rather than VC here
        isAlone = isAlone == null
        if (!isAlone && potentialPraShortcut != null) {
            val a = parseAffix(potentialPraShortcut.first, potentialPraShortcut.second, precision, ignoreDefault)
            when {
                a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${a.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${a.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${a.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
            }
            if (potentialPraShortcut.first == RTI_AFFIX_CONSONANT)
                rtiScope = rtiScope ?: "{Ca}"
            firstSegment += a.plusSeparator()
        }
        var c = groups[k]
        if (c.startsWith("'") && k == i) {
            c = c.drop(1)
        }
        var v: String
        if (k + 3 <= j && groups[k+2] matches "'?y|'".toRegex()) { // Standalone end of slot VIII or Type 2 "delineation"
            v = when {
                "y" in groups[k+2] -> groups[k+1] + "y" + groups[k+3]
                groups[k+1] eq groups[k+3] -> groups[k+1]
                else -> groups[k+1] + groups[k+3]
            }
            k += 2
        } else if (k + 2 == j) { // Something went wrong in vowel parsing, by now groups[k+2] should be a consonant
            return error("Affix group (slot VIII) ended unexpectedly")
        } else { // Standard CV
            v = groups[k+1]
        }
        if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
            return error("'$c' can't be a valid affix consonant")
        val a = parseAffix(c, v, precision, ignoreDefault, canBePraShortcut = isAlone)
        when {
            a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${a.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
            a.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${a.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
            a.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${a.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
            a == PRA_SHORTCUT_AFFIX_MARKER && potentialPraShortcut == null -> {
                potentialPraShortcut = c to v
                k += 2
                continue
            }
        }
        firstSegment += a.plusSeparator()
        if (c == RTI_AFFIX_CONSONANT)
            rtiScope = rtiScope ?: "{Stm}"
        k += 2
    }
    if (potentialPraShortcut != null && isAlone == true) { // PRA shortcut
        val shortcut = parsePraShortcut(potentialPraShortcut.first, potentialPraShortcut.second, precision)
                ?: return error("Unknown personal referent: '" + potentialPraShortcut.first + "'")
        firstSegment += shortcut.plusSeparator()
    }
    sentenceParsingState?.rtiAffixScope = null
    sentenceParsingState?.isLastFormativeVerbal = stress == 0
    return if (secondSegment.isEmpty() || secondSegment.startsWith(SLOT_SEPARATOR)) {
        firstSegment.dropLast(SLOT_SEPARATOR.length) + if (specialAffixJoin) {
            SPECIAL_AFFIX_SLOT_SEPARATOR + secondSegment.drop(SLOT_SEPARATOR.length)
        } else {
            secondSegment
        }
    } else {
        if (specialAffixJoin) {
            firstSegment.dropLast(SLOT_SEPARATOR.length) + SPECIAL_AFFIX_SLOT_SEPARATOR
        } else {
            firstSegment
        } + secondSegment
    }
}

fun parseModular(groups: Array<String>,
                 precision: Int,
                 ignoreDefault: Boolean,
                 verbalFormative: Boolean? = false,
                 modularForcedStress: Int? = null,
                 sentenceParsingState: SentenceParsingState? = null): String {
    var stress = modularForcedStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1
    var i = 0
    var result = ""
    if (groups[0] == "w" || groups[0] == "y") {
        result += "{Incp}".plusSeparator()
        i++
    }
    while (i + 2 < groups.size && i < 7) {
        if (groups[i+1].startsWith("h") || groups[i+1] == "y" && groups.getOrNull(i+3)?.startsWith("h") == true) {
            val vn = when {
                groups.getOrNull(i+1) == "y" -> {
                    i += 2
                    groups[i-2] + "y" + groups[i]
                }
                else -> groups[i]
            }
            val cn = when (verbalFormative) {
                true -> Mood.byCn(groups[i+1])?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(groups[i+1])?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(groups[i+1])?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(groups[i+1])?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood: ${groups[i]}")
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                    ?: return error("Unknown valence/phase/level/effect: $vn")
            result += join(patternOne, cn).plusSeparator()
        } else if (groups[i+1].startsWith("'h")) {
            val cnString = groups[i+1].trimGlottal()
            val cn = when (verbalFormative) {
                true -> Mood.byCn(cnString)?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(cnString)?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood: ${groups[i]}")
            val vt = Aspect.byVowel(groups[i]) ?: return error("Unknown aspect: ${groups[i]}")
            result += join(vt.toString(precision, ignoreDefault), cn).plusSeparator()
        } else {
            assert(groups[i+1] matches "'?[wy]".toRegex()
                    || groups[i+1] == "y"
                        && !groups[i].endsWith("u")
                        && groups.getOrNull(i+3)?.matches("'?[wy]".toRegex()) == true
            )
            val vn = when {
                groups[i+1] == "y" && !groups[i].endsWith("u") -> {
                    i += 2
                    groups[i-2] + "y" + groups[i]
                }
                else -> groups[i]
            }
            val fncCn = if (groups[i].defaultForm().endsWith("u")) {
                "y"
            } else {
                "w"
            }
            val contextIndex = listOf(fncCn, "'w", "'y").indexOf(groups[i+1])
            if (contextIndex == -1)
                return error("Expected the Cn value to be $fncCn, but found '${groups[i+1]}'")
            val context = Context.values()[contextIndex + 1]
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                    ?: return error("Unknown phase/context: $vn")
            result += join(patternOne, context.toString(precision, ignoreDefault)).plusSeparator()
        }
        i += 2
    }
    val valence = i > 1 && stress == 1
    result += when {
        valence -> parseVnPatternOne(groups[i], precision, ignoreDefault)
                ?: return error("Unknown valence/context: ${groups[i]}")
        i > 1 && stress == 0 -> parseModularScope(groups[i], precision, ignoreDefault)
        else -> Aspect.byVowel(groups[i])?.toString(precision, ignoreDefault)
                ?: return error("Unknown aspect: ${groups[i]}")
    }
    sentenceParsingState?.rtiAffixScope = null
    return if (result.endsWith(SLOT_SEPARATOR)) result.dropLast(SLOT_SEPARATOR.length) else result
}

fun parsePRA(groups: Array<String>,
             precision: Int,
             ignoreDefault: Boolean,
             sentenceParsingState: SentenceParsingState? = null): String {
    var stress = sentenceParsingState?.forcedStress ?: groups.findStress()
    if (stress == -1)
        stress = 1
    var result = ""
    var i = 0
    val refA = if (groups[1] == "ë" && groups.size >= 4 && !groups[2].isModular()) {
        i += 2
        groups[0] + groups[2]
    } else {
        groups[0]
    }
    i++
    val ref = parseFullReferent(refA, precision, ignoreDefault) ?: return error("Unknown referent: $refA")
    result += ref.plusSeparator(sep = CATEGORY_SEPARATOR)
    val v1 = if (i + 2 < groups.size && groups[i+1] == "'") {
        i += 2
        groups[i-2] + "'" + groups[i]
    } else {
        groups[i]
    }
    i++
    result += if (stress == 0) {
        parseVk(v1)?.toString(precision) ?: return error("Unknown illocution/expectation/validation: $v1")
    } else {
        Case.byVowel(v1)?.toString(precision) ?: return error("Unknown case vowel: $v1")
    }
    if (i + 1 < groups.size) {
        assert(groups[i] == "w" || groups[i] == "y")
        val v2 = if (i + 3 < groups.size && groups[i+2] == "'") {
            i += 2
            groups[i-1] + "'" + groups[i+1]
        } else {
            groups[i+1]
        }
        i += 2
        val case = Case.byVowel(v2)?.toString(precision) ?: return error("Unknown case vowel: $v2")
        if (i < groups.size) {
            if (!(i + 1 == groups.size || groups[i+1] == "ë" && i + 2 == groups.size))
                return error("PRA ended unexpectedly: ${groups.joinToString("")}")
            result += (parseFullReferent(groups[i], precision, ignoreDefault, final = true)
                    ?: return error("Unknown referent: ${groups[i]}")).plusSeparator(start = true)
            result += case.plusSeparator(start = true, sep = CATEGORY_SEPARATOR)
        } else {
            result += case.plusSeparator(start = true, sep = CATEGORY_SEPARATOR)
        }
    } else if (i + 1 == groups.size) { // Slot III isn't there all the way
        return error("PRA ended unexpectedly: ${groups.joinToString("")}")
    }
    sentenceParsingState?.rtiAffixScope = null
    return result
}

fun parseCombinationPRA(groups: Array<String>,
                        precision: Int,
                        ignoreDefault: Boolean,
                        sentenceParsingState: SentenceParsingState? = null): String {
    var stress = sentenceParsingState?.forcedStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    var result = ""
    var i = 0
    if (groups[0].isVowel()) {
        val (vv, _) = parseSimpleVv(groups[0]) ?: return error("Unknown Vv value: ${groups[0]}")
        result += vv.toString(precision, ignoreDefault).plusSeparator()
        i++
    }
    result += (parseFullReferent(groups[i], precision, ignoreDefault)
            ?: return error("Unknown referent: ${groups[i]}"))
            .plusSeparator(sep = CATEGORY_SEPARATOR)
    val case = if (groups[i+2] == "'") {
        i += 2
        groups[i-1] + "'" + groups[i+1]
    } else {
        groups[i+1]
    }
    result += Case.byVowel(case)?.toString(precision) ?: return error("Unknown case value: $case")
    val specIndex = COMBINATION_PRA_SPECIFICATION.indexOf(groups[i+2]) // Cannot be -1 because of preemptive check
    result += Specification.values()[specIndex].toString(precision, ignoreDefault)
    i += 3 // Beginning of affixes
    if (groups.size - i == 0) { // No affixes, no slot 6
        return result
    } else {
        val slotSixFilled = (groups.size - i) % 2
        val limit = groups.size - slotSixFilled
        while (i < limit) {
            if (i + 1 >= limit) {
                return error("Affix group of combination PRA ended unexpectedly")
            }
            val v = if (groups[i+1] == "y") {
                i += 2
                groups[i-2] + "y" + groups[i]
            } else if (groups[i+1] == "'") { // Can only happen with slot VI being Series 5+
                if (i+3 > limit && slotSixFilled == 1) break else return error("Affix group of combination PRA ended unexpectedly")
            } else {
                groups[i]
            }
            val c = groups[i+1]
            if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
                return error("'$c' can't be a valid affix consonant")
            val a = parseAffix(c, v, precision, ignoreDefault)
            when {
                a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${a.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${a.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${a.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
            }
            result += a.plusSeparator(start = true)
            i += 2
        }
        if (slotSixFilled == 1) {
            val last = if (i+2 == groups.lastIndex && groups[i+1] == "'") { // Series 5+ case
                groups[i] + "'" + groups[i+2]
            } else if (i != groups.lastIndex) {
                return error("Combination PRA ended unexpectedly")
            } else {
                groups[i]
            }
            result += when {
                stress == 2 && last == "a" -> return result
                stress == 1 -> Case.byVowel(last)?.toString(precision)
                        ?: return error("Unknown case value: $last")
                else -> parseVk(last)?.toString(precision)
                        ?: return error("Unknown illocution/sanction: $last")
            }.plusSeparator(start = true)
        }
    }
    sentenceParsingState?.rtiAffixScope = null
    return result
}

fun parseAffixualScoping(groups: Array<String>,
                         precision: Int,
                         ignoreDefault: Boolean,
                         sentenceParsingState: SentenceParsingState? = null): String {
    var rtiScope: String? = sentenceParsingState?.rtiAffixScope
    var result = ""
    var i = 0
    if (groups[0] == "ë")
        i++
    var c = groups[i]
    var v: String
    if (groups[i+2] == "y") {
        v = groups[i+1] + "y" + groups[i+3]
        i += 4
    } else {
        v = groups[i+1]
        i += 2
    }
    if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
        return error("'$c' can't be a valid affix consonant")
    var aff = parseAffix(c, v, precision, ignoreDefault)
    when {
        aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
    }
    result += aff.plusSeparator()
    val scope = affixAdjunctScope(groups[i], ignoreDefault) ?: return error("Invalid scope: ${groups[i]}")
    if (c == RTI_AFFIX_CONSONANT)
        rtiScope = rtiScope ?: scope
    result += scope.plusSeparator()
    i++
    while (i + 2 <= groups.size) {
        if (groups[i+1] == "y") {
            if (i + 3 >= groups.size) {
                return error("Second affix group ended unexpectedly")
            }
            v = groups[i] + "y" + groups[i+2]
            c = groups[i+3]
            i += 4
        } else {
            v = groups[i]
            c = groups[i+1]
            i += 2
        }
        if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
            return error("'$c' can't be a valid affix consonant")
        aff = parseAffix(c, v, precision, ignoreDefault)
        when {
            aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel: ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
            aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel: ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
            aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster: ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
        }
        if (c == RTI_AFFIX_CONSONANT)
            rtiScope = rtiScope ?: ""
        result += aff
        i += 2
    }
    val sc = affixAdjunctScope(groups.getOrNull(i), ignoreDefault, scopingAdjunctVowel = true)
    if (sc != "" && rtiScope == "")
        rtiScope = sc
    result += (sc ?: return error("Invalid scope: ${groups[i]}")).plusSeparator(start = true)
    val stress = sentenceParsingState?.forcedStress ?: groups.findStress()
    result += when (stress) {
        0 -> "{Incp}".plusSeparator(start = true)
        1 -> ""
        else -> return error("Couldn't parse stress: stress was on syllable $stress from the end")
    }
    if (rtiScope != null)
        sentenceParsingState?.rtiAffixScope = rtiScope
    return result
}

fun parseAffixual(groups: Array<String>,
                  precision: Int,
                  ignoreDefault: Boolean,
                  sentenceParsingState: SentenceParsingState? = null): String {
    var rtiScope = sentenceParsingState?.rtiAffixScope
    var stress = sentenceParsingState?.forcedStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    var i = 0
    val v = if (groups[1] == "y") {
        i += 2
        groups[0] + "y" + groups[2]
    } else {
        groups[0]
    }
    val c = groups[i+1]
    if (c.isInvalidLexical() && v != CA_STACKING_VOWEL)
        return error("'$c' can't be a valid affix consonant")
    val aff = parseAffix(c, v, precision, ignoreDefault)
    val scope = affixAdjunctScope((groups.getOrNull(i)?.defaultForm()), ignoreDefault)
    return when {
        aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> error("Unknown affix vowel: ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> error("Unknown case vowel: ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> error("Unknown Ca cluster: ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
        scope == null -> error("Invalid scope: ${groups[i+2]}")
        else -> {
            if (c == RTI_AFFIX_CONSONANT)
                rtiScope = rtiScope ?: scope
            if (rtiScope != null)
                sentenceParsingState?.rtiAffixScope = rtiScope
            aff + scope.plusSeparator(start = true) + if (stress != 1) {
                "{Incp}".plusSeparator(start = true)
            } else {
                ""
            }
        }
    }
}

fun error(s: String) = "\u0000" + s

fun errorList(s: String) = listOf("\u0000", s)
