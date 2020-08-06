@file:Suppress("IMPLICIT_CAST_TO_ANY")

package io.github.syst3ms.tnil

import java.io.PrintWriter
import java.io.StringWriter

fun main() {
    println(parseWord("alẓalörsürwu’ö", 1, true))
}

fun parseSentence(s: String, precision: Int, ignoreDefault: Boolean) : List<String> {
    if (s.isBlank()) {
        return errorList("Nothing to parse.")
    }
    val words = s.toLowerCase()
        .split("\\s+".toRegex())
    var modular : Pair<Int, Int?>? = null
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
                toParse = toParse.substring(1)
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
            val res = parseWord(toParse, precision, ignoreDefault, forcedStress)
            if (res.startsWith("}")) {
                if (word.startsWith("ë")) { // Carrier end
                    if (!carrier)
                        return errorList("*Syntax error* : '$word' doesn't mark the end of any carrier root/adjunct.")
                    carrier = false
                    result += "\" "
                } else {
                    val reg = Register.valueOf(res.substring(1))
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
                modular = i to forcedStress
                continue
            } else if (res.startsWith("\u0000")) {
                return errorList("**Parsing error** : ${res.substring(1)}")
            } else if (res.endsWith("{{")) {
                concatenative = true
            } else if (res.endsWith("\"")) {
                carrier = true
            } else if (word.startsWith("_")) {
                result += "$res } "
                continue
            } else if (res.contains("carrier") || res.contains("**s**")) { // Carrier root
                carrier = true
                result += "$res \" "
                continue
            } else if (res.endsWith("{")) {
                register = Register.valueOf(res.dropLast(1))
                result += register.toString(precision) + " { "
                continue
            } else if (modular != null) { // Now we can know the stress of the formative and finally parse the adjunct properly
                val mod = parseModular(
                        words[modular.first].splitGroups(),
                        precision,
                        ignoreDefault,
                        word.splitGroups().findStress() < 1,
                        modular.second
                )
                if (mod.startsWith("\u0000")) {
                    return errorList("**Parsing error** : ${mod.substring(1)}")
                }
                result += "$mod "
                modular = null
            }
            forcedStress = null
            result += "$res "
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
    if (modular != null) {
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

fun parseWord(s: String, precision: Int, ignoreDefault: Boolean, stress: Int? = null, alone: Boolean = false): String {
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
    } else if (groups.size >= 2 && groups[0].isVowel() && groups[1].startsWith("'")) { // Affixual scoping adjunct
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
        parseFormative(groups, precision, ignoreDefault, stress)
    }
}

fun parseFormative(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
    val stress = forceStress ?: groups.findStress().coerceAtLeast(0)
    var firstSegment = ""
    var i = 0
    var possibleSpecialShortForm = false
    /*
     * First value is 0 for -N- and 1 for -D-
     * Second value is the stem
     */
    var parsingReferentRoot : Pair<Int, Int>? = null
    // First we need to determine if the formative is short, simple or complex
    if (groups[0] in CD_CONSONANTS) { // Complex formative
        if (groups.size < 7) { // Minimum possible for a complex formative
            return error("Complex formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var rootFlag = 0
        val (cd, shortVf) = parseCd(groups[0])
        if (cd[0] == Designation.FORMAL)
            rootFlag = 4
        val vv = parseVvComplex(groups[3])
        if (vv != null) {
            rootFlag = rootFlag or ((vv[3] as Enum<*>).ordinal + 1) % 4
        }
        val vf = Case.byVowel(groups[1], shortVf) ?: return error("Unknown case value : ${groups[1]}")
        var stem = rootFlag and 3
        if (groups[2].isInvalidLexical())
            return error("'${groups[2]}' can't be a valid root consonant")
        val ci = parseRoot(groups[2], precision, stem, rootFlag and 4 == 4)
        firstSegment += cd.toString(precision, ignoreDefault, designationUsed = ci.third).plusSeparator()
        firstSegment += vf.toString(precision, false).plusSeparator()
        firstSegment += ci.first.plusSeparator()  // Not gonna bother with the special case of referent roots here
        if (vv?.get(0) == Designation.FORMAL)
            rootFlag = 4
        val vr = parseVr(groups[5])
        if (vr != null) {
            rootFlag = rootFlag or ((vr[1] as Enum<*>).ordinal + 1) % 4
        }
        stem = rootFlag and 3
        if (groups[4].isInvalidLexical())
            return error("'${groups[4]}' can't be a valid root consonant")
        val cr = parseRoot(groups[4], precision, stem, rootFlag and 4 == 4)
        firstSegment += (vv?.toString(precision, ignoreDefault, designationUsed = cr.third) ?: return error("Unknown Vv value : ${groups[3]}")).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[4] == "n") {
            parsingReferentRoot = 0 to stem
            "@-"
        } else if (precision > 0 && stem != 0 && groups[4] == "d") {
            parsingReferentRoot = 1 to stem
            "@-"
        } else {
            cr.first.plusSeparator()
        }
        firstSegment += (vr?.toString(precision, ignoreDefault, stemUsed = cr.second) ?: return error("Unknown Vr value : ${groups[5]}")).plusSeparator()
        i += 6
    } else if (groups[0].isConsonant() && groups.getOrNull(2) != "h") { // Short formative
        if (groups.size < 3) { // Minimum possible for a short formative
            return error("Short formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var rootFlag = 0
        var shortVv : List<Precision>? = null
        val vr: String?
        if (groups.size >= 5 && groups[2] matches "'h?|h".toRegex()) {
            shortVv = when (groups[2]) {
                "'h" -> {
                    rootFlag = 4
                    listOf(Designation.FORMAL, Version.COMPLETIVE)
                }
                "'" -> {
                    rootFlag = 4
                    listOf(Designation.FORMAL)
                }
                else -> throw IllegalStateException()
            }
            vr = if (groups[1] == groups[3]) {
                groups[1]
            } else {
                groups[1] + groups[3]
            }
            i += 4
        } else if (groups[2].startsWith("'") || groups[2].startsWith("h") && groups[2].length > 1) {
            shortVv = when {
                // Infixation rules forbid 'h at the beginning of the next consonant
                groups[2].startsWith("'") -> {
                    rootFlag = 4
                    listOf(Designation.FORMAL)
                }
                groups[2].startsWith("h") -> {
                    listOf(Version.COMPLETIVE)
                }
                else -> throw IllegalStateException()
            }
            vr = groups[1]
            i += 2
        } else {
            vr = groups[1]
            i += 2
        }
        val v = parseVr(vr)
        if (v != null) {
            rootFlag = rootFlag or ((v[1] as Enum<*>).ordinal + 1) % 4
        }
        val stem = rootFlag and 3
        if (groups[0].isInvalidLexical())
            return error("'${groups[0]}' can't be a valid root consonant")
        val cr = parseRoot(groups[0], precision, stem, rootFlag and 4 == 4)
        firstSegment += (shortVv?.toString(precision, false, designationUsed = cr.third) ?: "").plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[0] == "n") {
            parsingReferentRoot = 0 to stem
            "@-"
        } else if (precision > 0 && stem != 0 && groups[0] == "d") {
            parsingReferentRoot = 1 to stem
            "@-"
        } else {
            cr.first.plusSeparator()
        }
        firstSegment += (v?.toString(precision, ignoreDefault, stemUsed = cr.second) ?: return error("Unknown Vr value : $vr")).plusSeparator()
    } else if (groups[0].isConsonant()) {
        possibleSpecialShortForm = true
        i += 2
    } else { // Simple formative
        if (groups.size < 4) {
            return error("Simple formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var rootFlag = 0
        val vv = parseVvSimple(groups[0])
        if (vv?.get(0) == Designation.FORMAL)
            rootFlag = 4
        val vr = parseVr(groups[2])
        if (vr != null) {
            rootFlag = rootFlag or ((vr[1] as Enum<*>).ordinal + 1) % 4
        }
        val stem = rootFlag and 3
        if (groups[1].isInvalidLexical())
            return error("'${groups[1]}' can't be a valid root consonant")
        val cr = parseRoot(groups[1], precision, stem, rootFlag and 4 == 4)
        firstSegment += (vv?.toString(precision, ignoreDefault, designationUsed = cr.third) ?: return error("Unknown Vv value : ${groups[0]}")).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[1] == "n") {
            parsingReferentRoot = 0 to stem
            "@-"
        } else if (precision > 0 && stem != 0 && groups[1] == "d") {
            parsingReferentRoot = 1 to stem
            "@-"
        } else {
            cr.first.plusSeparator()
        }
        firstSegment += (vr?.toString(precision, ignoreDefault, stemUsed = cr.second) ?: return error("Unknown Vr value : ${groups[2]}")).plusSeparator()
        i += 3
    }
    // i is now either at Ca or the beginning of Slot VII
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
        val vcvk = if (stress == 0 || stress == 3) {
            parseVk(v)?.toString(precision, ignoreDefault) ?: return error("Unknown illocution/expectation/validation : $v")
        } else {
            Case.byVowel(v)?.toString(precision, ignoreDefault) ?: return error("Unknown case vowel : $v")
        }.plusSeparator(start = true)
        secondSegment = "$vcvk$secondSegment"
        j--
    }
    var contextMarked = false
    if (j - i >= 2 && groups[j] matches "'?(h[lrwmn]?|[wy]|hlw)".toRegex()) { // Cn
        contextMarked = true // Anything in this block will either mark context(/valence) or imply it's EXS(/MNO)
        if (groups[j].startsWith("h")) {
            val cn = if (stress == 0 || stress == 3) {
                Mood.byCn(groups[j])
            } else {
                CaseScope.byCn(groups[j])
            } as Precision? ?: return error("Unknown case-scope/mood : ${groups[j]}")
            val vn = parseValenceContext(groups[j - 1]) ?: return error("Unknown valence/context : ${groups[j - 1]}")
            secondSegment = (vn.toString(precision, ignoreDefault).plusSeparator(sep = "/") + cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        } else if (groups[j].startsWith("'h") && groups[j] != "'hlw") {
            contextMarked = false
            val cnString = groups[j].trimGlottal()
            val cn = if (stress == 0 || stress == 3) {
                Mood.byCn(cnString)
            } else {
                CaseScope.byCn(cnString)
            } as Precision? ?: return error("Unknown case-scope/mood : $cnString")
            val vt = Aspect.byVowel(groups[j - 1]) ?: return error("Unknown aspect : ${groups[j - 1]}")
            secondSegment = (vt.toString(precision, ignoreDefault).plusSeparator(sep = "/") + cn.toString(precision, ignoreDefault)).plusSeparator(start = true) + secondSegment
        } else if (groups[j] == "w" || groups[j] == "y") {
            val vn = parsePhaseContext(groups[j - 1]) ?: return error("Unknown phase/context : ${groups[j - 1]}")
            secondSegment = vn.toString(precision, ignoreDefault).plusSeparator(start = true) + secondSegment
        } else if (groups[j] == "'w" || groups[j] == "'hlw") {
            val vn = parseLevelContext(groups[j - 1]) ?: return error("Unknown level/context : ${groups[j - 1]}")
            secondSegment = vn.toString(precision, ignoreDefault).plusSeparator(start = true) + (if (groups[j] == "'hlw" && precision > 0) "(abs)" else if (groups[j] == "'hlw") "a" else "") + secondSegment
        } else {
            assert(groups[j] == "'y")
            val vn = parseEffectContext(groups[j - 1], precision, ignoreDefault) ?: return error("Unknown effect/context : ${groups[j - 1]}")
            secondSegment = vn.plusSeparator(start = true) + secondSegment
        }
        j -= 2
    }
    if (secondSegment.isEmpty() && stress == 0) { // Ensure that CNF/PPS be always marked
        secondSegment = "-ASR/COG/OBS"
    }
    if (!contextMarked && (stress == 2 || stress == 3)) {
        secondSegment = "-FNC$secondSegment"
    }
    // j is now either at Ca, or at the end of Slot IX
    if (i == j) { // We're at Ca, slots VII and IX are empty
        if (possibleSpecialShortForm && groups[i] == "h") { // Special short form
            var rootFlag = 4
            val vr = groups[1]
            val v = parseVr(vr)
            if (v != null) {
                rootFlag = rootFlag or ((v[1] as Enum<*>).ordinal + 1) % 4
            }
            val stem = rootFlag and 3
            if (groups[0].isInvalidLexical())
                return error("'${groups[0]}' can't be a valid root consonant")
            val cr = parseRoot(groups[0], precision, stem, rootFlag and 4 == 4)
            firstSegment += Version.COMPLETIVE.toString(precision).plusSeparator()
            firstSegment += if (precision > 0 && stem != 0 && groups[0] == "n") {
                parsingReferentRoot = 0 to stem
                "@-"
            } else if (precision > 0 && stem != 0 && groups[0] == "d") {
                parsingReferentRoot = 1 to stem
                "@-"
            } else {
                cr.first.plusSeparator()
            }
            if (parsingReferentRoot?.first == 0) {
                val desc = animateReferentDescriptions[parsingReferentRoot.second - 1][0]
                firstSegment = firstSegment.replace("@", "'$desc'")
            } else if (parsingReferentRoot?.first == 1) {
                val desc = inanimateReferentDescriptions[parsingReferentRoot.second - 1][0]
                firstSegment = firstSegment.replace("@", "'$desc'")
            }
            firstSegment += (v?.toString(precision, ignoreDefault, stemUsed = cr.second) ?: return error("Unknown Vr value : $vr")).plusSeparator()
            return firstSegment.dropLast(1) + secondSegment
        }
        val c = groups[i]
        if (c.isGlottalCa())
            return error("The Ca group marks the end of Slot VII, but Slot VII is empty : $c")
        val ca = parseCa(c)
        val alternate = if (c.startsWith("x")) {
            if (stress == 0 || stress == 3) {
                Mood.byCn(c.replace('x', 'h'))
                        ?.toString(precision)
            } else {
                CaseScope.byCn(c.replace('x', 'h'))
                        ?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (ca != null) {
            if (parsingReferentRoot?.first == 0) {
                val desc = animateReferentDescriptions
                        .get(parsingReferentRoot.second - 1)
                        .get((ca[ca.size - 2] as Enum<*>).ordinal)
                firstSegment = firstSegment.replace("@", "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            } else if (parsingReferentRoot?.first == 1) {
                val desc = inanimateReferentDescriptions
                        .get(parsingReferentRoot.second - 1)
                        .get((ca[ca.size - 2] as Enum<*>).ordinal)
                firstSegment = firstSegment.replace("@", "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            }
        }
        return firstSegment.dropLast(1) +
                (caString ?: alternate ?: return error("Slot VIII is neither a valid Ca value nor a case-scope/mood : ${groups[i]}")).plusSeparator(start = true) +
                secondSegment
    } else if (groups[j].isGlottalCa() || groups[j-2] == "'") { // We're at Ca, slot IX is empty, but slot VII isn't
        val c = groups[j].drop(1)
        val ca = parseCa(c)
        val alternate = if (c.startsWith("x")) {
            if (stress == 0 || stress == 3) {
                Mood.byCn(c.replace('x', 'h'))
                        ?.toString(precision)
            } else {
                CaseScope.byCn(c.replace('x', 'h'))
                        ?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (ca != null) {
            if (parsingReferentRoot?.first == 0) {
                val desc = animateReferentDescriptions
                        .get(parsingReferentRoot.second - 1)
                        .get((ca[ca.size - 2] as Enum<*>).ordinal)
                firstSegment = firstSegment.replace("@", "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            } else if (parsingReferentRoot?.first == 1) {
                val desc = inanimateReferentDescriptions
                        .get(parsingReferentRoot.second - 1)
                        .get((ca[ca.size - 2] as Enum<*>).ordinal)
                firstSegment = firstSegment.replace("@", "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            }
        }
        secondSegment = (caString ?: (alternate ?: return error("Slot VIII is neither a valid Ca value nor a case-scope/mood : $c"))) + secondSegment
        j--
    } else {
        var caIndex = i
        for (k in j downTo i) {
            if (groups[k] == "'") { // End of slot VII, but the glottal stop isn't part of Ca
                caIndex = k+2
                break
            } else if (groups[k].isGlottalCa()) { // Ca reached
                caIndex = k
                break
            }
        }
        var unrecognized : String? = null
        val slotNine = groups.toList()
            .subList(caIndex+1, j+1)
            .chunked(2) {
                if (it.size == 1) {
                    unrecognized = "\u0000"
                    return@chunked null
                } else if (it[1].isInvalidLexical()) {
                    unrecognized = "#${it[1]}"
                    return@chunked null
                }
                val a = parseAffix(it[1], it[0], precision)
                if (a matches "^[#@&].+".toRegex()) {
                    unrecognized = a
                    return@chunked null
                }
                return@chunked a
            }
            .joinToString("-")
        if (unrecognized != null) {
            return when {
                unrecognized == "\u0000" -> error("Affix group (slot VII) ended unexpectedly")
                unrecognized!!.startsWith("#") -> error("'${unrecognized!!.substring(1)}' can't be a valid affix consonant")
                unrecognized!!.startsWith("&") -> error("Unknown case vowel : ${unrecognized!!.substring(1)}")
                unrecognized!!.startsWith("^") -> error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${unrecognized!!.substring(1)}")
                else -> error("Unknown affix vowel : ${unrecognized!!.substring(1)}")
            }
        }
        secondSegment = "-$slotNine$secondSegment"
        val c = if (groups[caIndex].isGlottalCa()) groups[caIndex].drop(1) else groups[caIndex]
        val ca = parseCa(c.trimH())
        val alternate = if (c.startsWith("x")) {
            if (stress == 0 || stress == 3) {
                Mood.byCn(c.replace('x', 'h'))
                        ?.toString(precision)
            } else {
                CaseScope.byCn(c.replace('x', 'h'))
                        ?.toString(precision)
            }
        } else null
        var caString = ca?.toString(precision, ignoreDefault)
        if (ca != null) {
            if (parsingReferentRoot?.first == 0) {
                val desc = animateReferentDescriptions
                        .get(parsingReferentRoot.second - 1)
                        .get((ca[ca.size - 2] as Enum<*>).ordinal)
                firstSegment = firstSegment.replace("@", "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            } else if (parsingReferentRoot?.first == 1) {
                val desc = inanimateReferentDescriptions
                        .get(parsingReferentRoot.second - 1)
                        .get((ca[ca.size - 2] as Enum<*>).ordinal)
                firstSegment = firstSegment.replace("@", "'$desc'")
                caString = caString?.replace("\\b[MPNA]\\b".toRegex(), "__$0__")
            }
        }
        secondSegment = (caString ?: (alternate ?: return error("Slot VIII is neither a valid Ca value nor a case-scope/mood : $c"))) + secondSegment
        j = caIndex-1
    }
    // j is now at the vowel before Ca
    var k = i
    if (possibleSpecialShortForm && groups[i] == "h") {
        var rootFlag = 4
        val vr = groups[1] + groups[3]
        val v = parseVr(vr)
        if (v != null) {
            rootFlag = rootFlag or ((v[1] as Enum<*>).ordinal + 1) % 4
        }
        val stem = rootFlag and 3
        val cr = parseRoot(groups[0], precision, stem, rootFlag and 4 == 4)
        firstSegment += Version.COMPLETIVE.toString(precision).plusSeparator()
        firstSegment += if (precision > 0 && stem != 0 && groups[0] == "n") {
            parsingReferentRoot = 0 to stem
            "@-"
        } else if (precision > 0 && stem != 0 && groups[0] == "d") {
            parsingReferentRoot = 1 to stem
            "@-"
        } else {
            cr.first.plusSeparator()
        }
        if (parsingReferentRoot?.first == 0) {
            val desc = animateReferentDescriptions[parsingReferentRoot.second - 1][0]
            firstSegment = firstSegment.replace("@", "'$desc'")
        } else if (parsingReferentRoot?.first == 1) {
            val desc = inanimateReferentDescriptions[parsingReferentRoot.second - 1][0]
            firstSegment = firstSegment.replace("@", "'$desc'")
        }
        firstSegment += (v?.toString(precision, ignoreDefault, stemUsed = cr.second) ?: return error("Unknown Vr value : $vr")).plusSeparator()
        k += 2
    }
    while (k <= j) { // Reminder : affixes are CV rather than VC here
        val c = groups[k]
        var v : String
        if (k + 3 <= j && (groups[k+2] == "'" || groups[k+2] == "w")) { // Standalone end of slot VII or Type 2 delineation
            v = when {
                groups[k+2] == "w" -> groups[k+1] + "w" + groups[k+3]
                groups[k+1] == groups[k+3] -> groups[k+1]
                else -> groups[k+1] + groups[k+3]
            }
            k += 2
        } else if (k + 2 == j) { // Something went wrong in vowel parsing, by now groups[k+2] should be a consonant
            return error("Affix group (slot XII) ended unexpectedly")
        } else { // Standard CV
            v = groups[k+1]
        }
        k += 2
        val aff = parseAffix(c, v, precision)
        when {
            c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
            aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.substring(1)}")
            aff.startsWith("&") -> return error("Unknown case vowel : ${aff.substring(1)}")
            else -> firstSegment += "$aff-"
        }
    }
    return if (secondSegment.isEmpty() || secondSegment.startsWith("-")) {
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
        result += "{Stm}-"
        i++
    }
    while (i+2 < groups.size && i < 7) {
        if (groups[i+1].startsWith("h")) {
            val cn = when (verbalFormative) {
                true -> Mood.byCn(groups[i+1])?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(groups[i+1])?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(groups[i+1])?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(groups[i+1])?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood : ${groups[i]}")
            val vn = parseValenceContext(groups[i]) ?: return error("Unknown valence/context : ${groups[i]}")
            result += (vn.toString(precision, ignoreDefault).plusSeparator() + cn).plusSeparator()
        } else if (groups[i+1].startsWith("'h")) {
            val cnString = groups[i+1].trimGlottal()
            val cn = when (verbalFormative) {
                true -> Mood.byCn(cnString)?.toString(precision, ignoreDefault)
                false -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)
                null -> CaseScope.byCn(cnString)?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|")?.plus(Mood.byCn(cnString)?.toString(precision, ignoreDefault))
            } ?: return error("Unknown case-scope/mood : ${groups[i]}")
            val vt = Aspect.byVowel(groups[i]) ?: return error("Unknown aspect : ${groups[i]}")
            result += (vt.toString(precision, ignoreDefault).plusSeparator() + cn).plusSeparator()
        } else if (groups[i+1] == "w" || groups[i+1] == "y") {
            val vn = parsePhaseContext(groups[i]) ?: return error("Unknown phase/context : ${groups[i]}")
            result += vn.toString(precision, ignoreDefault).plusSeparator()
        } else if (groups[i+1] == "'w") {
            val vn = parseLevelContext(groups[i]) ?: return error("Unknown level/context : ${groups[i]}")
            result += vn.toString(precision, ignoreDefault).plusSeparator()
        } else {
            assert(groups[i+1] == "'y")
            val vn = parseEffectContext(groups[i], precision, ignoreDefault)
                    ?: return error("Unknown effect/context : ${groups[i]}")
            result += vn.plusSeparator()
        }
        i += 2
    }
    val valence = i > 1 && stress == 1
    val vn = when {
        valence -> parseValenceContext(groups[i]) ?: return error("Unknown valence/context : ${groups[i]}")
        else -> listOf(Aspect.byVowel(groups[i]) ?: return error("Unknown aspect : ${groups[i]}"))
    }
    return result + vn.toString(precision, ignoreDefault)
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
    var i = 0
    if (groups[0].isVowel()) {
        result += (parseVvSimple(groups[0])?.toString(precision, ignoreDefault) ?: return error("Unknown Vv value : ${groups[0]}")).plusSeparator()
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
                } else if (it[1].defaultForm() in INVALID_LEXICAL_CONSONANTS) {
                    unrecognized = "#${it[1]}"
                    return@chunked null
                }
                val a = parseAffix(it[1], it[0], precision)
                if (a.startsWith("#") || a.startsWith("@"))
                    unrecognized = a
                return@chunked a
            }
            .joinToString("-")
            .plusSeparator(start = true)
        if (unrecognized != null) {
            return when {
                unrecognized == "\u0000" -> error("Affix group of combination PRA ended unexpectedly")
                unrecognized!!.startsWith("#") -> error("'${unrecognized!!.substring(1)}' cannot be an affix consonant")
                unrecognized!!.startsWith("&") -> error("Unknown case vowel : ${unrecognized!!.substring(1)}")
                unrecognized!!.startsWith("^") -> error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${unrecognized!!.substring(1)}")
                else -> error("Unknown affix vowel : ${unrecognized!!.substring(1)}")
            }
        }
        if (slotSixFilled == 1) {
            result += if (stress == 2 && groups.last() == "a") {
                return result.dropLast(1)
            } else if (stress == 1) {
                Case.byVowel(groups.last())?.toString(precision)
                    ?: return error("Unknown case value : ${groups.last()}")
            } else if (stress == 0) {
                parseVk(groups.last())?.toString(precision) ?: return error("Unknown illocution/sanction : ${groups.last()}")
            } else {
                return error("Couldn't parse slot 6 of combined PRA using stress : ${groups.last()}")
            }.plusSeparator(start = true)
        }
    }
    return result
}

fun parseAffixualScoping(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
    var result = ""
    var i = 0
    var v : String
    var c : String
    var aff: String
    if (groups[1] == "'") {
        v = if (groups[0] == groups[2]) groups[0] else groups[0] + groups[2]
        c = groups[3]
        i += 4
    } else {
        assert(groups[1].startsWith("'"))
        v = groups[0]
        c = groups[1].trimGlottal()
        i += 2
    }
    aff = parseAffix(c, v, precision)
    when {
        c.isInvalidLexical() -> return error("'${c}' can't be an affix consonant")
        aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.substring(1)}")
        aff.startsWith("&") -> return error("Unknown case vowel : ${aff.substring(1)}")
        aff.startsWith("^") -> return error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${aff.substring(1)}")
    }
    result += aff
    while (i+2 <= groups.size) {
        v = groups[i]
        c = groups[i+1]
        aff = parseAffix(c, v, precision)
        when {
            c.isInvalidLexical() -> return error("'${c}' can't be an affix consonant")
            aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.substring(1)}")
            aff.startsWith("&") -> return error("Unknown case vowel : ${aff.substring(1)}")
            aff.startsWith("^") -> return error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${aff.substring(1)}")
        }
        result += aff.plusSeparator(start = true)
        i += 2
    }
    val scope = if (i < groups.size) scopeToString(groups[i].defaultForm(), ignoreDefault) else ""
    result += (scope ?: return error("Invalid scope : ${groups[i]}")).plusSeparator(start = true)
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    result += when (stress) {
        0 -> "-FML"
        1 -> ""
        2 -> "-CPT"
        3 -> "-FML/CPT"
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
    val aff = parseAffix(c, v, precision)
    val scope = if (groups.size == 3) scopeToString(groups[2].defaultForm(), ignoreDefault) else ""
    return when {
        c.isInvalidLexical() -> error("'${c}' can't be a valid affix consonant")
        aff.startsWith("@") -> error("Unknown affix vowel : ${aff.substring(1)}")
        aff.startsWith("&") -> error("Unknown case vowel : ${aff.substring(1)}")
        aff.startsWith("^") -> error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${aff.substring(1)}")
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
