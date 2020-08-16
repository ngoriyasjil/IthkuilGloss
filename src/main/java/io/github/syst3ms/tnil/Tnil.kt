@file:Suppress("IMPLICIT_CAST_TO_ANY")

package io.github.syst3ms.tnil

import java.io.PrintWriter
import java.io.StringWriter

fun main() {
    println(parseSentence("altö eimalé", 1, true))
}

fun parseSentence(s: String, precision: Int, ignoreDefault: Boolean): List<String> {
    if (s.isBlank()) {
        return errorList("Nothing to parse.")
    }
    val words = s.toLowerCase()
            .split("\\s+".toRegex())
    var modularInfo: Pair<Int, Int?>? = null
    var forcedStress: Int? = null
    var register: Register? = null
    var concatenative = false
    var carrier = false
    var rtiAffixScope: String? = null
    val result = arrayListOf<String>()
    for ((i, word) in words.withIndex()) {
        var toParse = word
        if (!carrier) {
            if (toParse.startsWith(LOW_TONE_MARKER)) { // Register end
                if (register == null)
                    return errorList("*Syntax error* : low tone can't mark the end of non-default register, since no such register is active.")
                toParse = toParse.drop(1)
                register = null
            } else if (toParse matches "'[aeoui]'".toRegex()) {
                forcedStress = when (toParse[1]) {
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
            return errorList("**Parsing error** : '$word' contains non-Ithkuil characters")
        }
        try {
            var res = parseWord(toParse, precision, ignoreDefault, forcedStress, isInSentence = true, rtiAffixScope = rtiAffixScope)
            if (res.startsWith("§")) {
                rtiAffixScope = null
                res = res.drop(1)
            }
            if (res.startsWith("}")) {
                if (word.startsWith("ë")) { // Carrier end
                    if (!carrier)
                        return errorList("*Syntax error* : '$word' doesn't mark the end of any carrier root/adjunct.")
                    carrier = false
                    result += "$CARRIER_END "
                } else {
                    val reg = Register.valueOf(res.drop(1))
                    if (register == null) {
                        return errorList("*Syntax error* : '$word' doesn't mark the end of any active register.")
                    } else if (register != reg) {
                        return errorList("*Syntax error* : '$word' doesn't conclude the content of its corresponding initial adjunct.")
                    }
                    register = null
                    result += "$REGISTER_END "
                }
                continue
            } else if (carrier) {
                result += "$word "
                continue
            } else if (res == MODULAR_PLACEHOLDER) { // Modular adjunct
                modularInfo = i to forcedStress
                continue
            } else if (res.startsWith("\u0000")) {
                return errorList("**Parsing error** : ${res.drop(1)}")
            } else if (res.endsWith(CONCATENATIVE_START)) {
                concatenative = true
            } else if (res.endsWith(CARRIER_START)) {
                carrier = true
            } else if (word.startsWith(LOW_TONE_MARKER)) {
                result += "$res } "
                continue
            } else if ("carrier" in res || "**s**" in res) { // Carrier root
                carrier = true
                result += "$res $CARRIER_START "
                continue
            } else if (res.endsWith(REGISTER_START)) {
                register = Register.valueOf(res.dropLast(1))
                result += register.toString(precision) + " $REGISTER_START "
                continue
            } else if ((res.startsWith(NOMINAL_FORMATIVE_IDENTIFIER) || res.startsWith(VERBAL_FORMATIVE_IDENTIFIER))
                    && modularInfo != null) { // Now we can know the stress of the formative and finally parse the adjunct properly
                val mod = parseModular(
                        words[modularInfo.first].splitGroups(),
                        precision,
                        ignoreDefault,
                        res.startsWith(VERBAL_FORMATIVE_IDENTIFIER),
                        modularInfo.second
                )
                if (mod.startsWith("\u0000")) {
                    return errorList("**Parsing error** : ${mod.drop(1)}")
                }
                result.add(modularInfo.first, "$mod ")
                modularInfo = null
            } else if (res.contains(RTI_SCOPE_DATA_MARKER)) {
                rtiAffixScope = rtiAffixScope ?: res.substringBefore(RTI_SCOPE_DATA_MARKER)
                res = res.substringAfter(RTI_SCOPE_DATA_MARKER)
            }
            forcedStress = null
            result += (if (res.startsWith(NOMINAL_FORMATIVE_IDENTIFIER) || res.startsWith(VERBAL_FORMATIVE_IDENTIFIER)) res.drop(1) else res) + " "
        } catch (e: Exception) {
            if (carrier) {
                result += "$word "
                continue
            }
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
    if (modularInfo != null) {
        return errorList("A modular adjunct needs an adjacent formative. If you want to parse the adjunct by itself, use ??gloss, ??short or ??full.")
    }
    if (carrier) {
        result += CARRIER_END
    }
    if (register != null) {
        result += REGISTER_END
    }
    if (concatenative) {
        result += CONCATENATIVE_END
    }
    return result
}

fun parseWord(s: String,
              precision: Int,
              ignoreDefault: Boolean,
              stress: Int? = null,
              isInSentence: Boolean = false,
              rtiAffixScope: String? = null): String {
    val groups = s.splitGroups()
    // Easily-identifiable adjuncts
    if (groups.isEmpty())
        return error("Empty word")
    return if (groups.size == 1 && groups[0].isConsonant()) { // Bias adjunct
        Bias.byGroup(groups[0])?.toString(precision) ?: return error("Unknown bias : ${groups[0]}")
    } else if ((groups[0] == "ç" || groups[0] == "hr") && (groups.size == 2 || groups.size == 4 && groups[2] == "'")) { // Carrier & concatenative adjuncts
        val v = if (groups.size == 2) groups[1] else groups[1] + "'" + groups[3]
        (Case.byVowel(v)?.toString(precision) ?: return error("Unknown case value : $v")) + when {
            !isInSentence -> ""
            groups[0] == "ç" -> " $CARRIER_START"
            else -> " $CONCATENATIVE_START"
        }
    } else if (s matches "h[aeëoöiuü][iu]?".toRegex()) { // Register adjunct
        val (reg, initial) = Register.byVowel(groups.last()) ?: return error("Unknown register adjunct : $s")
        if (!isInSentence) {
            reg.toString(precision)
        } else {
            if (initial) {
                "$reg$REGISTER_START"
            } else {
                "$REGISTER_END$reg"
            }
        }
    } else if (groups.size == 2 && groups[0].isConsonant() && !groups[0].isModular() ||
            groups.size >= 4 && !groups[0].isModular() && (groups[1] == "ë" || groups[2] matches "[wy]".toRegex() || groups[2] == "'" && (groups.size == 4 || groups[4] matches "[wy]".toRegex()))) { // PRA
        parsePRA(groups, precision, ignoreDefault, stress, isInSentence)
    } else if (groups.size >= 4 && groups[0].isVowel() && groups[3] in COMBINATION_PRA_SPECIFICATION ||
            groups.size >= 3 && groups[0] !in CD_CONSONANTS && groups[2] in COMBINATION_PRA_SPECIFICATION ||
            groups.size >= 6 && groups[0].isVowel() && groups[3] == "'" && groups[5] in COMBINATION_PRA_SPECIFICATION ||
            groups.size >= 5 && groups[0] !in CD_CONSONANTS && groups[2] == "'" && groups[4] in COMBINATION_PRA_SPECIFICATION) { // Combination PRA
        parseCombinationPRA(groups, precision, ignoreDefault, stress, isInSentence)
    } else if (groups.size >= 5 && groups[0].isConsonant() && groups[2] in SCOPING_VALUES ||
            groups.size >= 7 && groups[0].isConsonant() && groups[2] == "y" && groups[4] in SCOPING_VALUES ||
            groups.size >= 6 && groups[0] == "ë" && groups[3] in SCOPING_VALUES ||
            groups.size >= 8 && groups[0] == "ë" && groups[3] == "y" && groups[5] in SCOPING_VALUES) { // Affixual scoping adjunct
        parseAffixualScoping(groups, precision, ignoreDefault, stress, rtiAffixScope = rtiAffixScope, isInSentence = isInSentence)
    } else if (groups.size in 2..3 && groups[1].isConsonant() && !groups[1].isModular()) { // Single affixual adjunct
        parseAffixual(groups, precision, ignoreDefault, stress, rtiAffixScope = rtiAffixScope, isInSentence = isInSentence)
    } else if (groups.all { it.isVowel() || it.isModular() }) { // Modular adjunct
        if (!isInSentence) {
            parseModular(groups, precision, ignoreDefault, verbalFormative = null)
        } else {
            MODULAR_PLACEHOLDER
        }
    } else {
        parseFormative(groups, precision, ignoreDefault, stress, isInSentence, rtiAffixScope = rtiAffixScope)
    }
}

fun parseFormative(groups: Array<String>,
                   precision: Int,
                   ignoreDefault: Boolean,
                   forceStress: Int? = null,
                   isInSentence: Boolean = false,
                   rtiAffixScope: String? = null): String {
    val stress = forceStress ?: groups.findStress().coerceAtLeast(0)
    var firstSegment = ""
    var i = 0
    var tppDegree: Int? = null
    var rtiScope = rtiAffixScope
    /*
     * 0 = no glottal stop is used for short-form FML
     * 1 = a normal glottal stop (at the beginning of a cluster) is used for short-form FML
     * 2 = a supposed glottal Ca is used for short-form FML
     */
    var shortFormGlottalUse = 0
    var referentParsingData: PersonalReferentParsingData? = null
    // First we need to determine if the formative is short, simple or complex
    if (groups[0] in CD_CONSONANTS) { // Complex formative
        if (groups.size < 7) { // Minimum possible for a complex formative
            return error("Complex formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var stackedPerspectiveIndex: Int? = null
        val (cd, cdFlag) = parseCd(groups[0])
        val vf = Case.byVowel(groups[1], cdFlag and ALT_VF_FORM == ALT_VF_FORM)
                ?: return error("Unknown case value : ${groups[1]}")
        firstSegment += cd.toString(precision, ignoreDefault).plusSeparator()
        firstSegment += vf.toString(precision).plusSeparator()
        i = 2 // Slot III begins at index 2
        if (cdFlag and SLOT_THREE_PRESENT == SLOT_THREE_PRESENT) {
            val limit = groups.size - 5 // Conservative upper bound
            var stop = false
            while (i < limit && !stop) {
                val c = groups[i]
                var v: String
                if (i + 3 < limit && groups[i + 2] matches "'y?|y".toRegex()) {
                    if ("y" in groups[i + 2]) {
                        v = groups[i + 1] + "y" + groups[i + 3]
                        stop = groups[i + 2] == "'y"
                    } else { // Single glottal stop
                        v = if (groups[i + 1] == groups[i + 3]) {
                            groups[i + 1]
                        } else {
                            groups[i + 1] + groups[i + 3]
                        }
                        stop = true
                    }
                    i += 4
                } else {
                    v = groups[i + 1]
                    stop = groups[i + 2].startsWith("'") || groups[i + 1] == CA_STACKING_VOWEL && groups[i + 2].isGlottalCa()
                    i += 2
                }
                val aff = parseAffix(c, v, precision, ignoreDefault, slotThree = true)
                when {
                    c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
                    aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel : ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                    aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel : ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                    aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster : ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
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
        val vv = parseVvComplex(groups[i + 1]) ?: return error("Unknown Vv value : ${groups[i + 1]}")
        var stem = ((vv[2] as Enum<*>).ordinal + 1) % 4
        if (groups[i].isInvalidLexical())
            return error("'${groups[i]}' can't be a valid root consonant")
        val ci = parseRoot(groups[i], precision, stem)
        if (precision > 0 && stem != 0 && stackedPerspectiveIndex != null && (groups[i] == "n" || groups[i] == "d")) {
            if (groups[i] == "n") {
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
            firstSegment += ci.first.plusSeparator()
        }
        val vr = parseVr(groups[i + 3]) ?: return error("Unknown Vr value : ${groups[i + 3]}")
        stem = ((vr[1] as Enum<*>).ordinal + 1) % 4
        if (groups[i + 2].isInvalidLexical())
            return error("'${groups[i + 2]}' can't be a valid root consonant")
        val cr = parseRoot(groups[i + 2], precision, stem)
        firstSegment += vv.toString(precision, ignoreDefault, stemUsed = ci.second).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[i + 2] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else if (precision > 0 && stem != 0 && groups[i + 2] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else {
            cr.first
        }.plusSeparator()
        firstSegment += vr.toString(precision, ignoreDefault, stemUsed = cr.second).plusSeparator()
        i += 4
    } else if (groups[0].isConsonant()) { // Short formative
        if (groups.size < 3) { // Minimum possible for a short formative
            return error("Short formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var shortVv = ""
        val vr: String?
        if (groups.size >= 5 && groups[2] == "'") {
            shortVv = Version.COMPLETIVE.toString(precision)
            vr = if (groups[1] == groups[3]) {
                groups[1]
            } else {
                groups[1] + groups[3]
            }
            i += 4
        } else {
            if (groups[2].isGlottalCa()) {
                shortVv = Version.COMPLETIVE.toString(precision)
                shortFormGlottalUse = if (groups[2].startsWith("'")) 1 else 2
            }
            vr = groups[1]
            i += 2
        }
        val v = parseVr(vr) ?: return error("Unknown Vr value : $vr")
        val stem = ((v[1] as Enum<*>).ordinal + 1) % 4
        if (groups[0].isInvalidLexical())
            return error("'${groups[0]}' can't be a valid root consonant")
        val cr = parseRoot(groups[0], precision, stem)
        firstSegment += shortVv.plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[0] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else if (precision > 0 && stem != 0 && groups[0] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else {
            cr.first
        }.plusSeparator()
        firstSegment += v.toString(precision, ignoreDefault, stemUsed = cr.second).plusSeparator()
    } else { // Simple formative
        if (groups.size < 4) {
            return error("Simple formative ended unexpectedly : ${groups.joinToString("")}")
        }
        val vvParse = if (groups[1] matches "'?[wy]".toRegex()) {
            i += 2
            groups[0] + groups[1] + groups[2]
        } else {
            groups[0]
        }
        val (vv, shortcutIndex) = parseVvSimple(vvParse) ?: return error("Unknown Vv value : $vvParse")
        val vr = parseVr(groups[i+2]) ?: return error("Unknown Vr value : ${groups[i+2]}")
        val stem = ((vr[2] as Enum<*>).ordinal + 1) % 4
        if (groups[i+1].isInvalidLexical())
            return error("'${groups[i+1]}' can't be a valid root consonant")
        val cr = parseRoot(groups[i+1], precision, stem)
        firstSegment += join(
                vv.toString(precision, ignoreDefault),
                if (shortcutIndex > 0) {
                    tppDegree = shortcutIndex + 1
                    TPP_SHORTCUT_PLACEHOLDER
                } else {
                    ""
                }
        ).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[i+1] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else if (precision > 0 && stem != 0 && groups[i+1] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            REFERENT_ROOT_PLACEHOLDER
        } else {
            cr.first
        }.plusSeparator()
        firstSegment += vr.toString(precision, ignoreDefault, stemUsed = cr.second).plusSeparator()
        i += 3
    }
    // i is now either at Ca or the beginning of Slot VIII
    var secondSegment = ""
    var j = groups.lastIndex
    // Start from the end to easily identify each slot
    val noGlottalTail = j >= 6 && groups[j - 1].isVowel() && groups[j - 2].startsWith("'")
    val glottalTail = j >= 6 && groups[j].startsWith("'")
    if (noGlottalTail || glottalTail) { // Bias
        val c = groups[j].trimGlottal()
        val bias = Bias.byGroup(c)
        val alternate = if (stress == 0 || stress == 3) {
            Mood.byCy(c)
        } else {
            CaseScope.byCy(c)
        } as Precision?
        secondSegment += (bias?.toString(precision)
                ?: alternate?.toString(precision)
                ?: return error("Unknown bias/case-scope/mood : $c")).plusSeparator(start = true)
        j--
    }
    if (groups[j].isVowel()) { // Vc/Vk
        val v = if (groups[j - 1] == "'") {
            j -= 2
            groups[j] + "'" + groups[j + 2]
        } else {
            groups[j]
        }
        val vcvk = if (stress == 0) {
            parseVk(v)?.toString(precision, ignoreDefault)
                    ?: return error("Unknown illocution/expectation/validation : $v")
        } else {
            Case.byVowel(v)?.toString(precision, ignoreDefault) ?: return error("Unknown case vowel : $v")
        }.plusSeparator(start = true)
        secondSegment = "$vcvk$secondSegment"
        j--
    }
    if (j - i >= 2 && groups[j] matches "('?[hw]|'y).+".toRegex()) { // Cn
        if (groups[j].startsWith("h")) {
            val cn = if (stress == 0) {
                Mood.byCn(groups[j])
            } else {
                CaseScope.byCn(groups[j])
            } as Precision? ?: return error("Unknown case-scope/mood : ${groups[j]}")
            val vn = when {
                groups[j - 2] == "y" -> {
                    j -= 2
                    groups[j - 1] + "y" + groups[j + 1]
                }
                else -> groups[j - 1]
            }
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                    ?: return error("Unknown valence/phase/level/effect : $vn")
            secondSegment = join(patternOne, cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        } else if (groups[j].startsWith("'h")) {
            val cnString = groups[j].trimGlottal()
            val cn = if (stress == 0) {
                Mood.byCn(cnString)
            } else {
                CaseScope.byCn(cnString)
            } as Precision? ?: return error("Unknown case-scope/mood : $cnString")
            val vt = Aspect.byVowel(groups[j - 1]) ?: return error("Unknown aspect : ${groups[j - 1]}")
            secondSegment = join(vt.toString(precision, ignoreDefault), cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        } else {
            assert(groups[j] matches "'?[wy]".toRegex())
            val fncCn = if (groups[j - 1].defaultForm().endsWith("u")) {
                "y"
            } else {
                "w"
            }
            val contextIndex = listOf(fncCn, "'w", "'y").indexOf(groups[j])
            if (contextIndex == -1)
                return error("Expected the Cn value to be $fncCn, but found '${groups[j]}'")
            val context = Context.values()[contextIndex + 1]
            val vn = when {
                groups[j - 2] == "y" -> {
                    j -= 2
                    groups[j - 1] + "y" + groups[j + 1]
                }
                else -> groups[j - 1]
            }
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                    ?: return error("Unknown phase/context : $vn")
            secondSegment = join(patternOne, context.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        }
        j -= 2
    }
    if (secondSegment.isEmpty() && stress == 0) { // Ensure that ASR/COG/OBS be always marked
        secondSegment = "ASR/COG/OBS".plusSeparator(start = true)
    }
    // j is now either at Ca, or at the end of Slot X
    if (i == j) { // We're at Ca, slots VIII and X are empty
        val c = groups[i]
        if (c.isGlottalCa() && shortFormGlottalUse != 2)
            return error("This Ca group marks the end of Slot VIII, but Slot VIII is empty : $c")
        val ca = parseCa(if (c.isGlottalCa()) c.drop(1) else c)
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
        if (firstSegment.contains(TPP_SHORTCUT_PLACEHOLDER) && tppDegree != null) {
            firstSegment = firstSegment.replace(TPP_SHORTCUT_PLACEHOLDER, tppAffixString(tppDegree, rtiScope, precision))
        }
        return when {
            stress == 0 && isInSentence -> FORMATIVE_SENTENCE_MARKER + VERBAL_FORMATIVE_IDENTIFIER
            isInSentence -> "$FORMATIVE_SENTENCE_MARKER:"
            else -> ""
        } + firstSegment.dropLast(SLOT_SEPARATOR.length) +
            (caString ?: alternate
                ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood : ${groups[i]}")).plusSeparator(start = true) +
            secondSegment
    } else if (groups[j].isGlottalCa() || groups[j - 2] == "'") { // We're at Ca, slot X is empty, but slot VIII isn't
        val c = groups[j].drop(1)
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
                ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood : $c"))) + secondSegment
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
        var k = caIndex + 1
        while (k <= j) {
            if (k + 1 > j) {
                return error("Affix group (slot VIII) ended unexpectedly")
            }
            val v = if (groups[k + 1] == "y") {
                k += 2
                groups[k - 2] + "y" + groups[k]
            } else if (groups[k + 1] == "'") { // Can only happen with case-stacking
                when {
                    k + 3 > j -> return error("Affix group (slot VIII) ended unexpectedly")
                    groups[k + 3] !in CASE_AFFIXES -> return error("Expected a case-stacking or case-accessor affix, but found the affix -${groups[k + 3]}- instead")
                    else -> {
                        k += 2
                        groups[k - 2] + "'" + groups[k]
                    }
                }
            } else {
                groups[k]
            }
            val c = groups[k + 1]
            if (c.isInvalidLexical()) {
                return error("'$c' can't be a valid affix consonant")
            }
            val a = parseAffix(c, v, precision, ignoreDefault)
            secondSegment = when {
                a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> error("Unknown affix vowel : ${a.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> error("Unknown case vowel : ${a.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                a.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> error("Unknown Ca cluster : ${a.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
                else -> a
            }.plusSeparator(start = true) + secondSegment
            if (c == RTI_AFFIX_CONSONANT)
                rtiScope = rtiScope ?: "{Ca}"
            k += 2
        }
        val c = if (groups[caIndex].isGlottalCa()) groups[caIndex].drop(1) else groups[caIndex]
        val ca = parseCa(c.trimH())
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
                ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood : $c"))) + secondSegment
        j = caIndex - 1
    }
    // j is now at the vowel before Ca
    var k = i
    while (k <= j) { // Reminder : affixes are CV rather than VC here
        var c = groups[k]
        if (c.startsWith("'") && k == i && shortFormGlottalUse == 1) {
            c = c.drop(1)
        } else if (c.startsWith("'")) {
            return error("Unexpected glottal stop : $c")
        }
        var v: String
        if (k + 3 <= j && groups[k + 2] matches "'?y".toRegex()) { // Standalone end of slot VIII or Type 2 "delineation"
            v = when {
                "y" in groups[k + 2] -> groups[k + 1] + "y" + groups[k + 3]
                c in CASE_AFFIXES -> groups[k + 1] + "'" + groups[k + 3]
                groups[k + 1] == groups[k + 3] -> groups[k + 1]
                else -> groups[k + 1] + groups[k + 3]
            }
            k += 2
        } else if (k + 2 == j) { // Something went wrong in vowel parsing, by now groups[k+2] should be a consonant
            return error("Affix group (slot VIII) ended unexpectedly")
        } else { // Standard CV
            v = groups[k + 1]
        }
        val aff = parseAffix(c, v, precision, ignoreDefault)
        when {
            c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
            aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel : ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
            aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel : ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
            aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster : ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
            else -> firstSegment += aff.plusSeparator()
        }
        if (c == RTI_AFFIX_CONSONANT)
            rtiScope = rtiScope ?: "{Stm}"
        k += 2
    }
    if (firstSegment.contains(TPP_SHORTCUT_PLACEHOLDER) && tppDegree != null) {
        firstSegment = firstSegment.replace(TPP_SHORTCUT_PLACEHOLDER, tppAffixString(tppDegree, rtiScope, precision))
    }
    secondSegment += Context.values()[stress].toString(precision, ignoreDefault).plusSeparator(start = true)
    return when {
        stress == 0 && isInSentence -> FORMATIVE_SENTENCE_MARKER + VERBAL_FORMATIVE_IDENTIFIER
        isInSentence -> "$FORMATIVE_SENTENCE_MARKER:"
        else -> ""
    } + if (secondSegment.isEmpty() || secondSegment.startsWith(SLOT_SEPARATOR)) {
        firstSegment.dropLast(SLOT_SEPARATOR.length) + secondSegment
    } else {
        firstSegment + secondSegment
    }
}

fun parseModular(groups: Array<String>,
                 precision: Int,
                 ignoreDefault: Boolean,
                 verbalFormative: Boolean? = false,
                 forceStress: Int? = null,
                 isInSentence: Boolean = false): String {
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1
    var i = 0
    var result = ""
    if (groups[0] == "w" || groups[0] == "y") {
        result += "{Incp}".plusSeparator()
        i++
    }
    while (i + 2 < groups.size && i < 7) {
        if (groups[i + 1].startsWith("h")) {
            val cn = when (verbalFormative) {
                true -> Mood.byCn(groups[i + 1])?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(groups[i + 1])?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(groups[i + 1])?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(groups[i + 1])?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood : ${groups[i]}")
            val vn = when {
                groups.getOrNull(i - 1) == "y" -> {
                    i += 2
                    groups[i - 2] + "y" + groups[i]
                }
                else -> groups[i]
            }
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault)
                    ?: return error("Unknown valence/phase/level/effect : $vn")
            result += join(patternOne, cn).plusSeparator()
        } else if (groups[i + 1].startsWith("'h")) {
            val cnString = groups[i + 1].trimGlottal()
            val cn = when (verbalFormative) {
                true -> Mood.byCn(cnString)?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(cnString)?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood : ${groups[i]}")
            val vt = Aspect.byVowel(groups[i]) ?: return error("Unknown aspect : ${groups[i]}")
            result += join(vt.toString(precision, ignoreDefault), cn).plusSeparator()
        } else {
            return error("Invalid Cn group for a modular adjunct : " + groups[i] + groups[i + 1])
        }
        i += 2
    }
    val valence = i > 1 && stress == 1
    result += when {
        valence -> parseVnPatternOne(groups[i], precision, ignoreDefault)
                ?: return error("Unknown valence/context : ${groups[i]}")
        else -> Aspect.byVowel(groups[i])?.toString(precision, ignoreDefault)
                ?: return error("Unknown aspect : ${groups[i]}")
    }
    return (if (isInSentence) "§" else "") + if (result.endsWith(SLOT_SEPARATOR)) result.dropLast(SLOT_SEPARATOR.length) else result
}

fun parsePRA(groups: Array<String>,
             precision: Int,
             ignoreDefault: Boolean,
             forceStress: Int? = null,
             isInSentence: Boolean = false): String {
    var stress = forceStress ?: groups.findStress()
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
    val ref = parseFullReferent(refA, precision, ignoreDefault) ?: return error("Unknown referent : $refA")
    result += ref.plusSeparator(sep = CATEGORY_SEPARATOR)
    val v1 = if (i + 2 < groups.size && groups[i + 1] == "'") {
        i += 2
        groups[i - 2] + "'" + groups[i]
    } else {
        groups[i]
    }
    i++
    result += if (stress == 0) {
        parseVk(v1)?.toString(precision) ?: return error("Unknown illocution/expectation/validation : $v1")
    } else {
        Case.byVowel(v1)?.toString(precision) ?: return error("Unknown case vowel : $v1")
    }
    if (i + 1 < groups.size) {
        assert(groups[i] == "w" || groups[i] == "y")
        val v2 = if (i + 3 < groups.size && groups[i + 2] == "'") {
            i += 2
            groups[i - 1] + "'" + groups[i + 1]
        } else {
            groups[i + 1]
        }
        i += 2
        val case = Case.byVowel(v2)?.toString(precision) ?: return error("Unknown case vowel : $v2")
        if (i < groups.size) {
            if (!(i + 1 == groups.size || groups[i + 1] == "ë" && i + 2 == groups.size))
                return error("PRA ended unexpectedly : ${groups.joinToString("")}")
            result += (parseFullReferent(groups[i], precision, ignoreDefault, final = true)
                    ?: return error("Unknown referent : ${groups[i]}")).plusSeparator(start = true)
            result += case.plusSeparator(start = true, sep = CATEGORY_SEPARATOR)
        } else {
            result += case.plusSeparator(start = true, sep = CATEGORY_SEPARATOR)
        }
    } else if (i + 1 == groups.size) { // Slot III isn't there all the way
        return error("PRA ended unexpectedly : ${groups.joinToString("")}")
    }
    return (if (isInSentence) "§" else "") + result
}

fun parseCombinationPRA(groups: Array<String>,
                        precision: Int,
                        ignoreDefault: Boolean,
                        forceStress: Int? = null,
                        isInSentence: Boolean = false): String {
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    var result = ""
    var i = 0
    if (groups[0].isVowel()) {
        val (vv, _) = parseVvSimple(groups[0]) ?: return error("Unknown Vv value : ${groups[0]}")
        result += vv.toString(precision, ignoreDefault).plusSeparator()
        i++
    }
    result += (parseFullReferent(groups[i], precision, ignoreDefault)
            ?: return error("Unknown referent : ${groups[i]}"))
            .plusSeparator(sep = CATEGORY_SEPARATOR)
    result += (Case.byVowel(groups[i + 1])?.toString(precision)
            ?: return error("Unknown case value : ${groups[i + 1]}"))
            .plusSeparator()
    val specIndex = COMBINATION_PRA_SPECIFICATION.indexOf(groups[i + 2]) // Cannot be -1 because of preemptive check
    result += Specification.values()[specIndex].toString(precision, ignoreDefault)
    i += 3 // Beginning of affixes
    if (groups.size - i == 0) { // No affixes, no slot 6
        return result
    } else {
        val slotSixFilled = (groups.size - i) % 2
        var unrecognized: String? = null
        result += groups.toList()
                .subList(i, groups.size - slotSixFilled)
                .chunked(2) {
                    if (it.size == 1) {
                        unrecognized = "\u0000"
                        return@chunked null
                    } else if (it[1].isInvalidLexical()) {
                        unrecognized = "$AFFIX_NON_LEXICAL_MARKER${it[1]}"
                        return@chunked null
                    }
                    val a = parseAffix(it[1], it[0], precision, ignoreDefault)
                    if (a.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER)
                            || a.startsWith(AFFIX_UNKNOWN_CASE_MARKER)
                            || a.startsWith(AFFIX_UNKNOWN_CA_MARKER)) {
                        unrecognized = a
                        return@chunked null
                    }
                    return@chunked a
                }
                .joinToString(SLOT_SEPARATOR)
                .plusSeparator(start = true)
        if (unrecognized != null) {
            val u = unrecognized!!
            return when {
                u == "\u0000" -> error("Affix group of combination PRA ended unexpectedly")
                u.startsWith(AFFIX_NON_LEXICAL_MARKER) -> error("'$u' can't be a valid affix consonant")
                u.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel : ${u.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
                u.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel : ${u.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
                else -> return error("Unknown Ca cluster : ${u.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
            }
        }
        if (slotSixFilled == 1) {
            result += if (stress == 2 && groups.last() == "a") {
                return result
            } else if (stress == 1) {
                Case.byVowel(groups.last())?.toString(precision)
                        ?: return error("Unknown case value : ${groups.last()}")
            } else {
                parseVk(groups.last())?.toString(precision)
                        ?: return error("Unknown illocution/sanction : ${groups.last()}")
            }.plusSeparator(start = true)
        }
    }
    return (if (isInSentence) "§" else "") + result
}

fun parseAffixualScoping(groups: Array<String>,
                         precision: Int,
                         ignoreDefault: Boolean,
                         forceStress: Int? = null,
                         rtiAffixScope: String? = null,
                         isInSentence: Boolean = false): String {
    var rtiScope: String? = rtiAffixScope
    var result = ""
    var i = 0
    if (groups[0] == "ë")
        i++
    var c: String
    var v: String
    var aff: String
    if (groups[i + 2] == "y") {
        c = groups[i]
        v = groups[i + 1] + "y" + groups[i + 3]
        i += 4
    } else {
        c = groups[i]
        v = groups[i + 1]
        i += 2
    }
    aff = parseAffix(c, v, precision, ignoreDefault)
    when {
        c.isInvalidLexical() -> return error("'${c}' can't be an affix consonant")
        aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel : ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel : ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster : ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
    }
    result += aff.plusSeparator()
    val scope = scopeToString(groups[i], ignoreDefault) ?: return error("Invalid scope : ${groups[i]}")
    if (c == RTI_AFFIX_CONSONANT)
        rtiScope = rtiScope ?: scope
    result += scope.plusSeparator()
    i++
    while (i + 2 <= groups.size) {
        if (groups[i + 1] == "y") {
            if (i + 3 >= groups.size) {
                return error("Second affix group ended unexpectedly")
            }
            v = groups[i] + "y" + groups[i + 2]
            c = groups[i + 3]
            i += 4
        } else {
            v = groups[i]
            c = groups[i + 1]
            i += 2
        }
        aff = parseAffix(c, v, precision, ignoreDefault)
        when {
            c.isInvalidLexical() -> return error("'${c}' can't be an affix consonant")
            aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> return error("Unknown affix vowel : ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
            aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> return error("Unknown case vowel : ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
            aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> return error("Unknown Ca cluster : ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
        }
        if (c == RTI_AFFIX_CONSONANT)
            rtiScope = rtiScope ?: ""
        result += aff
        i += 2
    }
    val sc = if (i < groups.size) scopeToString(groups[i], ignoreDefault) else ""
    if (sc != "" && rtiScope == "")
        rtiScope = sc
    result += (sc ?: return error("Invalid scope : ${groups[i]}")).plusSeparator(start = true)
    val stress = forceStress ?: groups.findStress()
    result += when (stress) {
        0 -> "{Incp}".plusSeparator(start = true)
        1 -> ""
        else -> return error("Couldn't parse stress : stress was on syllable $stress from the end")
    }
    return (if (isInSentence && rtiScope != null) rtiScope + RTI_SCOPE_DATA_MARKER else "") + result
}

fun parseAffixual(groups: Array<String>,
                  precision: Int,
                  ignoreDefault: Boolean,
                  forceStress: Int? = null,
                  rtiAffixScope: String? = null,
                  isInSentence: Boolean = false): String {
    var rtiScope = rtiAffixScope
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    val v = groups[0]
    val c = groups[1]
    val aff = parseAffix(c, v, precision, ignoreDefault)
    val scope = if (groups.size == 3) scopeToString(groups[2].defaultForm(), ignoreDefault) else ""
    return when {
        c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
        aff.startsWith(AFFIX_UNKNOWN_VOWEL_MARKER) -> error("Unknown affix vowel : ${aff.drop(AFFIX_UNKNOWN_VOWEL_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CASE_MARKER) -> error("Unknown case vowel : ${aff.drop(AFFIX_UNKNOWN_CASE_MARKER.length)}")
        aff.startsWith(AFFIX_UNKNOWN_CA_MARKER) -> error("Unknown Ca cluster : ${aff.drop(AFFIX_UNKNOWN_CA_MARKER.length)}")
        scope == null -> error("Invalid scope : ${groups[2]}")
        else -> {
            if (c == RTI_AFFIX_CONSONANT)
                rtiScope = rtiScope ?: scope
            (if (isInSentence && rtiScope != null) rtiScope + RTI_SCOPE_DATA_MARKER else "") + aff + scope.plusSeparator(start = true) + if (stress != 1) {
                "{Incp}".plusSeparator(start = true)
            } else {
                ""
            }
        }
    }
}

fun error(s: String) = "\u0000" + s

fun errorList(s: String) = listOf("\u0000", s)
