@file:Suppress("IMPLICIT_CAST_TO_ANY")

package io.github.syst3ms.tnil

import java.io.PrintWriter
import java.io.StringWriter

fun main() {
    println(parseWord("ařtüř", 1, true, alone = true))
}

fun parseSentence(s: String, precision: Int, ignoreDefault: Boolean) : List<String> {
    if (s.isBlank()) {
        return errorList("Nothing to parse.")
    }
    val words = s.toLowerCase()
        .split("\\s+".toRegex())
    var modularInfo : Pair<Int, Int?>? = null
    var forcedStress : Int? = null
    var register : Register? = null
    var concatenative = false
    var carrier = false
    val result = arrayListOf<String>()
    for ((i, word) in words.withIndex()) {
        var toParse = word
        if (!carrier) {
            if (toParse.startsWith("_")) { // Register end
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
            val res = parseWord(toParse, precision, ignoreDefault, forcedStress, isInSentence = true)
            if (res.startsWith("}")) {
                if (word.startsWith("ë")) { // Carrier end
                    if (!carrier)
                        return errorList("*Syntax error* : '$word' doesn't mark the end of any carrier root/adjunct.")
                    carrier = false
                    result += "\" "
                } else {
                    val reg = Register.valueOf(res.drop(1))
                    if (register == null) {
                        return errorList("*Syntax error* : '$word' doesn't mark the end of any active register.")
                    } else if (register != reg) {
                        return errorList("*Syntax error* : '$word' doesn't conclude the content of its corresponding initial adjunct.")
                    }
                    register = null
                    result += "} "
                }
                continue
            } else if (carrier) {
                result += "$word "
                continue
            } else if (res == "@") { // Modular adjunct
                modularInfo = i to forcedStress
                continue
            } else if (res.startsWith("\u0000")) {
                return errorList("**Parsing error** : ${res.drop(1)}")
            } else if (res.endsWith("{{")) {
                concatenative = true
            } else if (res.endsWith("\"")) {
                carrier = true
            } else if (word.startsWith("_")) {
                result += "$res } "
                continue
            } else if ("carrier" in res || "**s**" in res) { // Carrier root
                carrier = true
                result += "$res \" "
                continue
            } else if (res.endsWith("{")) {
                register = Register.valueOf(res.dropLast(1))
                result += register.toString(precision) + " { "
                continue
            } else if ((res.startsWith(":") || res.startsWith("!")) && modularInfo != null) { // Now we can know the stress of the formative and finally parse the adjunct properly
                val mod = parseModular(
                        words[modularInfo.first].splitGroups(),
                        precision,
                        ignoreDefault,
                        res.startsWith("!"),
                        modularInfo.second
                )
                if (mod.startsWith("\u0000")) {
                    return errorList("**Parsing error** : ${mod.drop(1)}")
                }
                result.add(modularInfo.first, "$mod ")
                modularInfo = null
            }
            forcedStress = null
            result += (if (res.startsWith("!") || res.startsWith(":")) res.drop(1) else res) + " "
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
        result += "\" "
    }
    if (register != null) {
        result += "}"
    }
    if (concatenative) {
        result += "}}"
    }
    return result
}

fun parseWord(s: String, precision: Int, ignoreDefault: Boolean, stress: Int? = null, alone: Boolean = false, isInSentence: Boolean = false): String {
    val groups = s.splitGroups()
    // Easily-identifiable adjuncts
    if (groups.isEmpty())
        return error("Empty word")
    return if (groups.size == 1 && groups[0].isConsonant()) { // Bias adjunct
        Bias.byGroup(groups[0])?.toString(precision) ?: return error("Unknown bias : ${groups[0]}")
    } else if ((groups[0] == "ç" || groups[0] == "hr") && (groups.size == 2 || groups.size == 4 && groups[2] == "'")) { // Carrier & concatenative adjuncts
        val v = if (groups.size == 2) groups[1] else groups[1] + "'" + groups[3]
        (Case.byVowel(v)?.toString(precision) ?: return error("Unknown case value : $v")) + when {
            alone -> ""
            groups[0] == "ç" -> " \""
            else -> " {{"
        }
    } else if (s matches "h[aeëoöiuü][iu]?".toRegex()) { // Register adjunct
        val (reg, initial) = Register.byVowel(groups.last()) ?: return error("Unknown register adjunct : $s")
        if (alone) {
            reg.toString(precision)
        } else {
            if (initial) {
                "$reg{"
            } else {
                "}$reg"
            }
        }
    } else if (groups.size == 2 && groups[0].isConsonant() && !groups[0].isModular() ||
            groups.size >= 4 && !groups[0].isModular() && (groups[1] == "ë" || groups[2] matches "[wy]".toRegex() || groups[2] == "'" && (groups.size == 4 || groups[4] matches "[wy]".toRegex()))) { // PRA
        parsePRA(groups, precision, ignoreDefault, stress)
    } else if (groups.size >= 4 && groups[0].isVowel() && groups[3] in combinationPRASpecification ||
            groups.size >= 3 && groups[0] !in CD_CONSONANTS && groups[2] in combinationPRASpecification ||
            groups.size >= 6 && groups[0].isVowel() && groups[3] == "'" && groups[5] in combinationPRASpecification ||
            groups.size >= 5 && groups[0] !in CD_CONSONANTS && groups[2] == "'" && groups[4] in combinationPRASpecification) { // Combination PRA
        parseCombinationPRA(groups, precision, ignoreDefault, stress)
    } else if (groups.size >= 5 && groups[0].isConsonant() && groups[2] in affixualScopingConsonants ||
            groups.size >= 7 && groups[0].isConsonant() && groups[2] == "y" && groups[4] in affixualScopingConsonants ||
            groups.size >= 6 && groups[0] == "ë" && groups[3] in affixualScopingConsonants ||
            groups.size >= 8 && groups[0] == "ë" && groups[3] == "y" && groups[5] in affixualScopingConsonants) { // Affixual scoping adjunct
        parseAffixualScoping(groups, precision, ignoreDefault, stress)
    } else if (groups.size in 2..3 && groups[1].isConsonant() && !groups[1].isModular()) { // Single affixual adjunct
        parseAffixual(groups, precision, ignoreDefault, stress)
    } else if (groups.all { it.isVowel() || it.isModular() }) { // Modular adjunct
        if (alone) {
            parseModular(groups, precision, ignoreDefault, verbalFormative = null)
        } else {
            "@"
        }
    } else {
        parseFormative(groups, precision, ignoreDefault, stress, isInSentence)
    }
}

fun parseFormative(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null, isInSentence: Boolean = false): String {
    val stress = forceStress ?: groups.findStress().coerceAtLeast(0)
    var firstSegment = ""
    var i = 0
    var verbal = false
    /*
     * null = the formative isn't short-form
     * 0 = no glottal stop is used for short-form FML
     * 1 = a normal glottal stop (at the beginning of a cluster) is used for short-form FML
     * 2 = a supposed glottal Ca is used for short-form FML
     */
    var shortFormGlottalUse : Int? = null
    var referentParsingData : PersonalReferentParsingData? = null
    // First we need to determine if the formative is short, simple or complex
    if (groups[0] in CD_CONSONANTS) { // Complex formative
        if (groups.size < 7) { // Minimum possible for a complex formative
            return error("Complex formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var stackedPerspectiveIndex : Int? = null
        val (cd, cdFlag) = parseCd(groups[0])
        val vf = Case.byVowel(groups[1], cdFlag and ALT_VF_FORM == ALT_VF_FORM) ?: return error("Unknown case value : ${groups[1]}")
        firstSegment += cd.toString(precision, ignoreDefault).plusSeparator()
        firstSegment += vf.toString(precision).plusSeparator()
        i = 2 // Slot III begins at index 2
        if (cdFlag and SLOT_THREE_PRESENT == SLOT_THREE_PRESENT) {
            val limit = groups.size - 5 // Conservative upper bound
            var stop = false
            while (i < limit && !stop) {
                val c = groups[i]
                var v : String
                if (i + 3 < limit && groups[i+2] matches "'y?|y".toRegex()) {
                    if ("y" in groups[i+2]) {
                        v = groups[i+1] + "y" + groups[i+3]
                        stop = groups[i+2] == "'y"
                    } else { // Single glottal stop
                        v = if (groups[i+1] == groups[i+3]) {
                            groups[i+1]
                        } else {
                            groups[i+1] + groups[i+3]
                        }
                        stop = true
                    }
                    i += 4
                } else {
                    v = groups[i+1]
                    stop = groups[i+2].startsWith("'") || groups[i+1] == CA_STACKING_VOWEL && groups[i+2].isGlottalCa()
                    i += 2
                }
                val aff = parseAffix(c, v, precision, ignoreDefault, slotThree = true)
                when {
                    c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
                    aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.drop(1)}")
                    aff.startsWith("&") -> return error("Unknown case vowel : ${aff.drop(1)}")
                    aff.contains("#") -> { // We handle the case of personal referents if the affix is Ca-stacking
                        if (stackedPerspectiveIndex == null) {
                            stackedPerspectiveIndex = aff.substringBefore('#').toInt()
                        }
                        firstSegment += aff.substringAfter('#') + "-"
                    }
                    else -> firstSegment += "$aff-"
                }
            }
        }
        val (vv, vrb) = parseVvComplex(groups[i+1]) ?: return error("Unknown Vv value : ${groups[i+1]}")
        verbal = vrb
        var stem = ((vv[2] as Enum<*>).ordinal + 1) % 4
        if (groups[i].isInvalidLexical())
            return error("'${groups[i]}' can't be a valid root consonant")
        val ci = parseRoot(groups[i], precision, stem)
        if (precision > 0 && stem != 0 && stackedPerspectiveIndex != null && (groups[i] == "n" || groups[i] == "d")) {
            if (groups[i] == "n") {
                val desc = animateReferentDescriptions[stem - 1][stackedPerspectiveIndex]
                // replaceFirst because there might be multiple stacked Ca
                firstSegment = firstSegment.replaceFirst("\\b[MPNA]\\b".toRegex(), "__$0__")
                firstSegment +="'$desc'-"
            } else {
                val desc = inanimateReferentDescriptions[stem - 1][stackedPerspectiveIndex]
                firstSegment = firstSegment.replaceFirst("\\b[MPNA]\\b".toRegex(), "__$0__")
                firstSegment += "'$desc'-"
            }
        } else {
            firstSegment += ci.first.plusSeparator()
        }
        val vr = parseVr(groups[i+3]) ?: return error("Unknown Vr value : ${groups[i+3]}")
        stem = ((vr[1] as Enum<*>).ordinal + 1) % 4
        if (groups[i+2].isInvalidLexical())
            return error("'${groups[i+2]}' can't be a valid root consonant")
        val cr = parseRoot(groups[i+2], precision, stem)
        firstSegment += vv.toString(precision, ignoreDefault, stemUsed = ci.second).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[i+2] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            "@"
        } else if (precision > 0 && stem != 0 && groups[i+2] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            "@"
        } else {
            cr.first
        }.plusSeparator()
        firstSegment += vr.toString(precision, ignoreDefault, stemUsed = cr.second).plusSeparator()
        i += 4
    } else if (groups[0].isConsonant()) { // Short formative
        if (groups.size < 3) { // Minimum possible for a short formative
            return error("Short formative ended unexpectedly : ${groups.joinToString("")}")
        }
        shortFormGlottalUse = 0
        var shortVv = ""
        val vr: String?
        if (groups.size >= 5 && groups[2] matches "'h?|h".toRegex()) {
            if (groups[2].startsWith("'"))
                verbal = true
            if (groups[2].endsWith("h"))
                shortVv = Version.COMPLETIVE.toString(precision)
            vr = if (groups[1] == groups[3]) {
                groups[1]
            } else {
                groups[1] + groups[3]
            }
            i += 4
        } else if (groups[2].isGlottalCa() || groups[2].startsWith("h") && groups[2].length > 1) {
            when {
                // Infixation rules forbid 'h at the beginning of the next consonant
                groups[2].isGlottalCa() -> {
                    shortFormGlottalUse = if (groups[2].startsWith("'")) 1 else 2
                    verbal = true
                }
                groups[2].startsWith("h") -> {
                    shortVv = Version.COMPLETIVE.toString(precision)
                }
                else -> throw IllegalStateException()
            }
            vr = groups[1]
            i += 2
        } else {
            vr = groups[1]
            i += 2
        }
        val v = parseVr(vr) ?: return error("Unknown Vr value : $vr")
        val stem = ((v[1] as Enum<*>).ordinal + 1) % 4 and 3
        if (groups[0].isInvalidLexical())
            return error("'${groups[0]}' can't be a valid root consonant")
        val cr = parseRoot(groups[0], precision, stem)
        firstSegment += shortVv.plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[0] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            "@-"
        } else if (precision > 0 && stem != 0 && groups[0] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            "@-"
        } else {
            cr.first.plusSeparator()
        }
        firstSegment += v.toString(precision, ignoreDefault, stemUsed = cr.second).plusSeparator()
    } else { // Simple formative
        if (groups.size < 4) {
            return error("Simple formative ended unexpectedly : ${groups.joinToString("")}")
        }
        val (vv, vrb) = parseVvSimple(groups[0])  ?: return error("Unknown Vv value : ${groups[0]}")
        verbal = vrb
        val vr = parseVr(groups[2]) ?: return error("Unknown Vr value : ${groups[2]}")
        val stem = ((vr[1] as Enum<*>).ordinal + 1) % 4
        if (groups[1].isInvalidLexical())
            return error("'${groups[1]}' can't be a valid root consonant")
        val cr = parseRoot(groups[1], precision, stem)
        firstSegment += vv.toString(precision, ignoreDefault).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[1] == "n") {
            referentParsingData = PersonalReferentParsingData(false, stem)
            "@-"
        } else if (precision > 0 && stem != 0 && groups[1] == "d") {
            referentParsingData = PersonalReferentParsingData(true, stem)
            "@-"
        } else {
            cr.first.plusSeparator()
        }
        firstSegment += vr.toString(precision, ignoreDefault, stemUsed = cr.second).plusSeparator()
        i += 3
    }
    // i is now either at Ca or the beginning of Slot VIII
    var secondSegment = ""
    var j = groups.lastIndex
    // Start from the end to easily identify each slot
    val noGlottalTail = j >= 6 && groups[j-1].isVowel() && groups[j-2].startsWith("'")
    val glottalTail = j >= 6 && groups[j].startsWith("'")
    if (noGlottalTail || glottalTail) { // Bias
        val c = groups[j].trimGlottal()
        val bias = Bias.byGroup(c)
        val alternate = if (stress == 0 || stress == 3) {
            Mood.byCy(c)
        } else {
            CaseScope.byCy(c)
        } as Precision?
        secondSegment += "-" + (bias?.toString(precision) ?: alternate?.toString(precision) ?: return error("Unknown bias/case-scope/mood : $c"))
        j--
    }
    if (groups[j].isVowel()) { // Vc/Vk
        val v = if (groups[j-1] == "'") {
            j -= 2
            groups[j] + "'" +  groups[j+2]
        } else {
            groups[j]
        }
        val vcvk = if (verbal) {
            parseVk(v)?.toString(precision, ignoreDefault) ?: return error("Unknown illocution/expectation/validation : $v")
        } else {
            Case.byVowel(v)?.toString(precision, ignoreDefault) ?: return error("Unknown case vowel : $v")
        }.plusSeparator(start = true)
        secondSegment = "$vcvk$secondSegment"
        j--
    }
    if (j - i >= 2 && groups[j] matches "('?[hw]|'y).+".toRegex()) { // Cn
        if (groups[j].startsWith("h")) {
            val cn = if (verbal) {
                Mood.byCn(groups[j])
            } else {
                CaseScope.byCn(groups[j])
            } as Precision? ?: return error("Unknown case-scope/mood : ${groups[j]}")
            val vn = when {
                groups[j-2] == "y" -> {
                    j -= 2
                    groups[j-1] + "y" + groups[j+1]
                }
                else -> groups[j-1]
            }
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault) ?: return error("Unknown valence/phase/level/effect : $vn")
            secondSegment = join(patternOne, cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        } else if (groups[j].startsWith("'h")) {
            val cnString = groups[j].trimGlottal()
            val cn = if (verbal) {
                Mood.byCn(cnString)
            } else {
                CaseScope.byCn(cnString)
            } as Precision? ?: return error("Unknown case-scope/mood : $cnString")
            val vt = Aspect.byVowel(groups[j - 1]) ?: return error("Unknown aspect : ${groups[j - 1]}")
            secondSegment = join(vt.toString(precision, ignoreDefault), cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        } else {
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
            val patternOne = parsePhaseContext(vn) ?: return error("Unknown phase/context : $vn")
            secondSegment = join(patternOne.toString(precision, ignoreDefault), context.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        }
        j -= 2
    }
    if (secondSegment.isEmpty() && verbal) { // Ensure that ASR/COG/OBS be always marked
        secondSegment = "-ASR/COG/OBS"
    }
    // j is now either at Ca, or at the end of Slot X
    if (i == j) { // We're at Ca, slots VIII and X are empty
        val c = groups[i]
        if (c.isGlottalCa() && (shortFormGlottalUse == null || shortFormGlottalUse == 0))
            return error("This Ca group marks the end of Slot VIII, but Slot VIII is empty : $c")
        val slotEight = if (c.isGlottalCa()) c.drop(1) else c.trimH()
        val ca = parseCa(slotEight)
        val alternate = if (slotEight != "h" && shortFormGlottalUse == null && slotEight.startsWith("h")
                || shortFormGlottalUse != null && slotEight.startsWith("x")) {
            if (verbal) {
                Mood.byCn(slotEight.replace('x', 'h'))?.toString(precision)
            } else {
                CaseScope.byCn(slotEight.replace('x', 'h'))?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (referentParsingData?.isInanimate == false) {
            val desc = animateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca) ?: 0]
            firstSegment = firstSegment.replace("@", "'$desc'")
            caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
        } else if (referentParsingData?.isInanimate == true) {
            val desc = inanimateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca) ?: 0]
            firstSegment = firstSegment.replace("@", "'$desc'")
            caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
        }
        return (if (verbal && isInSentence) "!" else if (isInSentence) ":" else "") +
                firstSegment.dropLast(1) +
                (caString ?: alternate ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood : ${groups[i]}")).plusSeparator(start = true) +
                secondSegment
    } else if (groups[j].isGlottalCa() || groups[j-2] == "'") { // We're at Ca, slot X is empty, but slot VIII isn't
        val c = groups[j].drop(1)
        val ca = parseCa(c)
        val alternate = if (c != "h" && shortFormGlottalUse == null && c.startsWith("h") || shortFormGlottalUse != null && c.startsWith("x")) {
            if (verbal) {
                Mood.byCn(c.replace('x', 'h'))?.toString(precision)
            } else {
                CaseScope.byCn(c.replace('x', 'h'))?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (referentParsingData?.isInanimate == false) {
            val desc = animateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca) ?: 0]
            firstSegment = firstSegment.replace("@", "'$desc'")
            caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
        } else if (referentParsingData?.isInanimate == true) {
            val desc = inanimateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca) ?: 0]
            firstSegment = firstSegment.replace("@", "'$desc'")
            caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
        }
        secondSegment = (caString ?: (alternate ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood : $c"))) + secondSegment
        j--
    } else {
        var caIndex = i
        for (k in j downTo i) {
            if (groups[k] == "'" || groups[k] == "'y") { // End of slot VIII, but the glottal stop isn't part of Ca
                caIndex = k+2
                break
            } else if (groups[k].isGlottalCa()) { // Ca reached
                caIndex = k
                break
            }
        }
        var k = caIndex + 1
        while (k <= j) {
            if (k+1 > j) {
                return error("Affix group (slot VIII) ended unexpectedly")
            }
            val v = if (groups[k+1] == "y") {
                k += 2
                groups[k-2] + "y" + groups[k]
            } else if (groups[k+1] == "'") { // Can only happen with case-stacking
                when {
                    k+3 > j -> return error("Affix group (slot VIII) ended unexpectedly")
                    groups[k+3] !in CASE_AFFIXES -> return error("Expected a case-stacking or case-accessor affix, but found the affix -${groups[k+3]}- instead")
                    else -> {
                        k += 2
                        groups[k-2] + "'" + groups[k]
                    }
                }
            } else {
                groups[k]
            }
            val c = groups[k+1]
            if (c.isInvalidLexical()) {
                return error("'$c' can't be a valid affix consonant")
            }
            val a = parseAffix(c, v, precision, ignoreDefault)
            secondSegment = when {
                a.startsWith("@") -> error("Unknown affix vowel : ${a.drop(1)}")
                a.startsWith("&") -> error("Unknown case vowel : ${a.drop(1)}")
                a.startsWith("^") -> error("Unknown Ca cluster : ${a.drop(1)}")
                else -> a
            }.plusSeparator(start = true) + secondSegment
            k += 2
        }
        val c = if (groups[caIndex].isGlottalCa()) groups[caIndex].drop(1) else groups[caIndex]
        val ca = parseCa(c.trimH())
        val alternate = if (c != "h" && shortFormGlottalUse == null && c.startsWith("h") || shortFormGlottalUse != null && c.startsWith("x")) {
            if (verbal) {
                Mood.byCn(c.replace('x', 'h'))?.toString(precision)
            } else {
                CaseScope.byCn(c.replace('x', 'h'))?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (referentParsingData?.isInanimate == false) {
            val desc = animateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca) ?: 0]
            firstSegment = firstSegment.replace("@", "'$desc'")
            caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
        } else if (referentParsingData?.isInanimate == true) {
            val desc = inanimateReferentDescriptions[referentParsingData.stem - 1][perspectiveIndexFromCa(ca) ?: 0]
            firstSegment = firstSegment.replace("@", "'$desc'")
            caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
        }
        secondSegment = (caString ?: (alternate ?: return error("Slot IX is neither a valid Ca value nor a case-scope/mood : $c"))) + secondSegment
        j = caIndex-1
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
        var v : String
        if (k + 3 <= j && groups[k+2] matches "'?y".toRegex()) { // Standalone end of slot VIII or Type 2 "delineation"
            v = when {
                "y" in groups[k+2] -> groups[k+1] + "y" + groups[k+3]
                c in CASE_AFFIXES -> groups[k+1] + "'" + groups[k+3]
                groups[k+1] == groups[k+3] -> groups[k+1]
                else -> groups[k+1] + groups[k+3]
            }
            k += 2
        } else if (k + 2 == j) { // Something went wrong in vowel parsing, by now groups[k+2] should be a consonant
            return error("Affix group (slot VIII) ended unexpectedly")
        } else { // Standard CV
            v = groups[k+1]
        }
        val aff = parseAffix(c, v, precision, ignoreDefault)
        when {
            c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
            aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.drop(1)}")
            aff.startsWith("&") -> return error("Unknown case vowel : ${aff.drop(1)}")
            aff.startsWith("^") -> return error("Unknown Ca cluster : ${aff.drop(1)}")
            else -> firstSegment += "$aff-"
        }
        k += 2
    }
    secondSegment += Context.values()[stress].toString(precision, ignoreDefault).plusSeparator(start = true)
    return (if (verbal && isInSentence) "!" else if (isInSentence) ":" else "") + if (secondSegment.isEmpty() || secondSegment.startsWith("-")) {
        firstSegment.dropLast(1) + secondSegment
    } else {
        firstSegment + secondSegment
    }
}

fun parseModular(groups: Array<String>, precision: Int, ignoreDefault: Boolean, verbalFormative: Boolean? = false, forceStress: Int? = null): String {
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1
    var i = 0
    var result = ""
    if (groups[0] == "w" || groups[0] == "y") {
        result += "{Incp}-"
        i++
    }
    while (i+2 < groups.size && i < 7) {
        if (groups[i+1].startsWith("h")) {
            val cn = when (verbalFormative) {
                true -> Mood.byCn(groups[i+1])?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(groups[i+1])?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(groups[i+1])?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(groups[i+1])?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood : ${groups[i]}")
            val vn = when {
                groups.getOrNull(i-1) == "y" -> {
                    i += 2
                    groups[i-2] + "y" + groups[i]
                }
                else -> groups[i]
            }
            val patternOne = parseVnPatternOne(vn, precision, ignoreDefault) ?: return error("Unknown valence/phase/level/effect : $vn")
            result += join(patternOne, cn).plusSeparator()
        } else if (groups[i+1].startsWith("'h")) {
            val cnString = groups[i+1].trimGlottal()
            val cn = when (verbalFormative) {
                true -> Mood.byCn(cnString)?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(cnString)?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood : ${groups[i]}")
            val vt = Aspect.byVowel(groups[i]) ?: return error("Unknown aspect : ${groups[i]}")
            result += join(vt.toString(precision, ignoreDefault), cn).plusSeparator()
        } else {
            return error("Invalid Cn group for a modular adjunct : " + groups[i] + groups[i+1])
        }
        i += 2
    }
    val valence = i > 1 && stress == 1
    result += when {
        valence -> parseVnPatternOne(groups[i], precision, ignoreDefault) ?: return error("Unknown valence/context : ${groups[i]}")
        else -> Aspect.byVowel(groups[i])?.toString(precision, ignoreDefault) ?: return error("Unknown aspect : ${groups[i]}")
    }
    return if (result.endsWith("-")) result.drop(1) else result
}

fun parsePRA(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
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
    result += ref.plusSeparator(sep = "/")
    val v1 = if (i+2 < groups.size && groups[i+1] == "'") {
        i += 2
        groups[i-2] + "'" + groups[i]
    } else {
        groups[i]
    }
    i++
    result += if (stress == 0) {
        parseVk(v1)?.toString(precision) ?: return error("Unknown illocution/expectation/validation : $v1")
    } else {
        Case.byVowel(v1)?.toString(precision) ?: return error("Unknown case vowel : $v1")
    }
    if (i+1 < groups.size) {
        assert(groups[i] == "w" || groups[i] == "y")
        val v2 = if (i+3 < groups.size && groups[i+2] == "'") {
            i += 2
            groups[i-1] + "'" + groups[i+1]
        } else {
            groups[i+1]
        }
        i += 2
        val case = Case.byVowel(v2)?.toString(precision) ?: return error("Unknown case vowel : $v2")
        if (i < groups.size) {
            if (!(i+1 == groups.size || groups[i+1] == "ë" && i+2 == groups.size))
                return error("PRA ended unexpectedly : ${groups.joinToString("")}")
            result += (parseFullReferent(groups[i], precision, ignoreDefault, final = true) ?: return error("Unknown referent : ${groups[i]}")).plusSeparator(start = true)
            result += case.plusSeparator(start = true, sep = "/")
        } else {
            result += case.plusSeparator(start = true, sep = "/")
        }
    } else if (i+1 == groups.size) { // Slot III isn't there all the way
        return error("PRA ended unexpectedly : ${groups.joinToString("")}")
    }
    return result
}

fun parseCombinationPRA(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    var result = ""
    var verbal = false
    var i = 0
    if (groups[0].isVowel()) {
        val (vv, vrb) = parseVvSimple(groups[0]) ?: return error("Unknown Vv value : ${groups[0]}")
        verbal = vrb
        result += vv.toString(precision, ignoreDefault).plusSeparator()
        i++
    }
    result += (parseFullReferent(groups[i], precision, ignoreDefault) ?: return error("Unknown referent : ${groups[i]}")) + "/"
    result += (Case.byVowel(groups[i+1])?.toString(precision) ?: return error("Unknown case value : ${groups[i+1]}")) + "-"
    val specIndex = combinationPRASpecification.indexOf(groups[i+2]) // Cannot be -1 because of preemptive check
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
                    unrecognized = "#${it[1]}"
                    return@chunked null
                }
                val a = parseAffix(it[1], it[0], precision, ignoreDefault)
                if (a.startsWith("#") || a.startsWith("@")) {
                    unrecognized = a
                    return@chunked null
                }
                return@chunked a
            }
            .joinToString("-")
            .plusSeparator(start = true)
        if (unrecognized != null) {
            return when {
                unrecognized == "\u0000" -> error("Affix group of combination PRA ended unexpectedly")
                unrecognized!!.startsWith("#") -> error("'${unrecognized!!.drop(1)}' cannot be an affix consonant")
                unrecognized!!.startsWith("&") -> error("Unknown case vowel : ${unrecognized!!.drop(1)}")
                unrecognized!!.startsWith("^") -> error("Unknown Ca cluster : ${unrecognized!!.drop(1)}")
                else -> error("Unknown affix vowel : ${unrecognized!!.drop(1)}")
            }
        }
        if (slotSixFilled == 1) {
            result += if (stress != 1 && groups.last() == "a") {
                return result
            } else if (!verbal) {
                Case.byVowel(groups.last())?.toString(precision)
                    ?: return error("Unknown case value : ${groups.last()}")
            } else {
                parseVk(groups.last())?.toString(precision)
                    ?: return error("Unknown illocution/sanction : ${groups.last()}")
            }.plusSeparator(start = true)
        }
    }
    return result
}

fun parseAffixualScoping(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
    var result = ""
    var i = 0
    if (groups[0] == "ë")
        i++
    var c : String
    var v : String
    var aff: String
    if (groups[i+2] == "y") {
        c = groups[i]
        v = groups[i+1] + "y" + groups[i+3]
        i += 4
    } else {
        c = groups[i]
        v = groups[i+1]
        i += 2
    }
    aff = parseAffix(c, v, precision, ignoreDefault)
    when {
        c.isInvalidLexical() -> return error("'${c}' can't be an affix consonant")
        aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.drop(1)}")
        aff.startsWith("&") -> return error("Unknown case vowel : ${aff.drop(1)}")
        aff.startsWith("^") -> return error("Unknown Ca cluster : ${aff.drop(1)}")
    }
    result += aff.plusSeparator()
    result += (scopeToString(groups[i], ignoreDefault) ?: return error("Invalid scope : ${groups[i]}")).plusSeparator()
    i++
    while (i+2 <= groups.size) {
        if (groups[i+1] == "y") {
            if (i+3 >= groups.size) {
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
        aff = parseAffix(c, v, precision, ignoreDefault)
        when {
            c.isInvalidLexical() -> return error("'${c}' can't be an affix consonant")
            aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.drop(1)}")
            aff.startsWith("&") -> return error("Unknown case vowel : ${aff.drop(1)}")
            aff.startsWith("^") -> return error("Unknown Ca cluster : ${aff.drop(1)}")
        }
        result += aff
        i += 2
    }
    val scope = if (i < groups.size) scopeToString(groups[i], ignoreDefault) else ""
    result += (scope ?: return error("Invalid scope : ${groups[i]}")).plusSeparator(start = true)
    val stress = forceStress ?: groups.findStress()
    result += when (stress) {
        0 -> "-{Incp}"
        1 -> ""
        else -> return error("Couldn't parse stress : stress was on syllable $stress from the end")
    }
    return result
}

fun parseAffixual(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    val v = groups[0]
    val c = groups[1]
    val aff = parseAffix(c, v, precision, ignoreDefault)
    val scope = if (groups.size == 3) scopeToString(groups[2].defaultForm(), ignoreDefault) else ""
    return when {
        c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
        aff.startsWith("@") -> error("Unknown affix vowel : ${aff.drop(1)}")
        aff.startsWith("&") -> error("Unknown case vowel : ${aff.drop(1)}")
        aff.startsWith("^") -> error("Unknown Ca cluster : ${aff.drop(1)}")
        scope == null -> error("Invalid scope : ${groups[2]}")
        else -> aff + scope.plusSeparator(start = true) + when (stress) {
            0 -> "-FML"
            1 -> ""
            2 -> "-CPT"
            3 -> "-FML/CPT"
            else -> return error("Couldn't parse stress : stress was on syllable $stress from the end")
        }
    }
}

fun error(s: String) = "\u0000" + s

fun errorList(s: String) = listOf("\u0000", s)
