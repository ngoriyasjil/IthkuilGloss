package io.github.syst3ms.tnil

import java.io.PrintWriter
import java.io.StringWriter

fun main() {
    println(parseWord("lalksthw", 1, true, alone = true))
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
    var interrogative = words.last().startsWith("/")
    var concatenative = false
    var carrier = false
    if (words.count { it.startsWith("/") } > 1) {
        return errorList("*Syntax error* : cannot mark an interrogative clause twice using rising tone. Please use IRG illocution instead.")
    }
    val result = arrayListOf<String>()
    for ((i, word) in words.withIndex()) {
        var toParse = word
        if (!carrier) {
            if (toParse.startsWith("/")) {
                toParse = toParse.substring(1)
            } else if (toParse.startsWith("_")) { // Register end
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
        if (toParse.any { it.toString().defaultForm() !in CONSONANTS && VOWEL_FORM.none { v -> v eq it.toString() } }) {
            if (carrier) {
                result += "$word "
                continue
            } else {
                return errorList("**Parsing error** : '$word' contains non-Ithkuil characters")
            }
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
            } else if (res.contains("carrier") || res.contains("*s*")) { // Carrier root
                carrier = true
                result += "$res \" "
                continue
            } else if (res.endsWith("{")) {
                register = Register.valueOf(res.dropLast(1))
                result += register.toString(precision) + " { "
                continue
            } else if (interrogative && (res.contains("CNF") || res.contains("confirmative"))) {
                interrogative = false
                result += res.replace("CNF", "IRG")
                    .replace("confirmative", "interrogative") + " "
                continue
            } else if (modular != null) { // Now we can know the stress of the formative and finally parse the adjunct properly
                val mod = parseModular(
                        words[modular.first].splitGroups(true),
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
    return if (groups.size == 2 && groups[0] == "h") { // Carrier adjunct
        (Case.byVowel(groups.last())?.toString(precision)
            ?: return error("Unknown case value : ${groups.last()}")) + (if (alone) "" else " \"")
    } else if (groups.size == 2 && groups[0] == "çç") { // Concatenative adjunct
        (Case.byVowel(groups.last())?.toString(precision)
            ?: return error("Unknown case value : ${groups.last()}")) + (if (alone) "" else " {{")
    } else if (s matches "[aeëoöiu]h[au]?".toRegex() && s != "ëha") { // Register adjunct, but we exclude the nonexistant "initial carrier-end"
        val reg = Register.byVowel(groups.first())!!
        if (alone) {
            reg.toString(precision)
        } else {
            if (groups.last() == "u") {
                "}$reg"
            } else {
                "$reg{"
            }
        }
    } else if (groups.size == 2 && !(groups[0] matches "[hwyç].?".toRegex()) && groups[1].isVowel() ||
        groups.size == 3 && groups[0].isVowel() && !(groups[1] matches "'?[hwy].?".toRegex()) ||
        groups.size == 5 && groups[2] == "ë"
    ) { // PRA
        parsePRA(groups, precision, stress)
    } else if (groups.size >= 4 && groups[0].isVowel() && combinationPRASpecification.contains(groups[3]) ||
            groups.size >= 3 && combinationPRASpecification.contains(groups[2])) { // Combination PRA
        parseCombinationPRA(groups, precision, ignoreDefault, stress)
    } else if (groups[0] == "çç" || groups[0] == "çw") { // Affixual scoping adjunct
        parseAffixualScoping(s.splitGroups(true), precision, ignoreDefault, stress)
    } else if (groups.size == 2 && groups[1].isConsonant() ||
        groups.size == 3 && groups[0] == "h" && groups[2].isConsonant() ||
        groups.size == 4 && (groups[0] == "h" || groups[0].isVowel() && groups[1] == "'") ||
        groups.size == 5 && groups[0] == "h" && groups[2] == "'" ||
        groups.size == 6 && groups[0] == "h" && groups[2] == "'" && groups[5] eq "a"
    ) { // Single affixual adjunct
        parseAffixual(groups, precision, ignoreDefault, stress)
    } else if (groups.all { it.isVowel() || it in MODULAR_CONSONANTS || it.length == 2 && it[0] == '\'' && (it[1] == 'w' || it[1] == 'y') }) { // Modular adjunct
        if (alone) {
            parseModular(s.splitGroups(true), precision, ignoreDefault, verbalFormative = null)
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
    val acceptableAlternateSlotEight = arrayOf("h", "hl", "hr", "hw", "hm", "hn")
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
        val cd = parseCd(groups[0])
        if (cd[0] == Designation.FORMAL)
            rootFlag = 1
        val vf = Case.byVowel(groups[1]) ?: return error("Unknown case value : ${groups[1]}")
        val ci = parseRoot(groups[2], precision, formal = rootFlag == 1)
        firstSegment += cd.toString(precision, ignoreDefault, designationUsed = ci.third).plusSeparator()
        firstSegment += vf.toString(precision, false).plusSeparator()
        firstSegment += ci.first.plusSeparator()  // Not gonna bother with the special case of referent roots here
        val vv = parseVv(groups[3])
        if (vv?.get(0) == Designation.FORMAL)
            rootFlag = 4
        val vr = parseVr(groups[5])
        if (vr != null) {
            rootFlag = rootFlag or ((vr[1] as Enum<*>).ordinal + 1) % 4
        }
        val stem = rootFlag and 3
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
    } else if (groups[0].isConsonant()) { // Short formative
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
                    acceptableAlternateSlotEight[0] = "ç"
                    listOf(Designation.FORMAL, Version.COMPLETIVE)
                }
                "h" -> {
                    acceptableAlternateSlotEight[0] = "ç"
                    listOf(Version.COMPLETIVE)
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
        } else if (groups[2].startsWith("'") || groups[2].startsWith("h") && groups[2].length > 1) { // h in Slot VIII is possible
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
    } else { // Simple formative
        if (groups.size < 4) {
            return error("Simple formative ended unexpectedly : ${groups.joinToString("")}")
        }
        var rootFlag = 0
        val vv = parseVv(groups[0])
        if (vv?.get(0) == Designation.FORMAL)
            rootFlag = 4
        val vr = parseVr(groups[2])
        if (vr != null) {
            rootFlag = rootFlag or ((vr[1] as Enum<*>).ordinal + 1) % 4
        }
        val stem = rootFlag and 3
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
    val noGlottalBias = j >= 6 && groups[j-1].isVowel() && (groups[j-1].contains("[wy]".toRegex()) ||
            groups[j-4].isVowel() && groups[j-3].isVowel() ||
            groups[j-3].isVowel() && groups[j-3].contains("[wy]".toRegex()) ||
            groups[j-3].isVowel() && groups[j-4] matches "[wy]".toRegex())
    val glottalBias = j >= 6 && groups[j].startsWith("'") && groups[j-2] matches "'?h.*".toRegex()
    if (!(groups[j] matches "'?h.*".toRegex()) && (noGlottalBias || glottalBias)) { // Bias
        val cb = groups[j].trimGlottal()
        secondSegment += "-" + (Bias.byGroup(cb)?.toString(precision) ?: return error("Unknown bias : $cb"))
        j--
    }
    if (groups[j].isVowel()) { // Vc/Vk
        val v = if (stress == 0 || stress == 3) {
            parseVk(groups[j])?.toString(precision, ignoreDefault) ?: return error("Unknown illocution/sanction : ${groups[j]}")
        } else {
            Case.byVowel(groups[j])?.toString(precision, ignoreDefault) ?: return error("Unknown case vowel : ${groups[j]}")
        }.plusSeparator(start = true)
        secondSegment = "$v$secondSegment"
        j--
    }
    var contextMarked = false
    if (j - i >= 2 && groups[j] matches "'?h.?".toRegex()) { // Case-scope/Mood
        contextMarked = true // Anything in this block will either mark context(/valence) or imply it's EXS(/MNO)
        secondSegment = if (stress == 0 || stress == 3) { // Mood
            (Mood.byGroup(groups[j].trimGlottal())?.toString(precision, ignoreDefault)
                ?: return error("Unknown mood : ${groups[j].trimGlottal()}")).plusSeparator(start = true) + (if (secondSegment.isEmpty()) "-CNF/PPS" else "")
            /*
             * CNF/PPS is always marked, THM isn't. This is actually directly opposite to what the language does.
             * Monosyllabic formatives (unmarked) are considered verbs, and THM must be marked.
             * However, since this tool would primarily be used for analysing single words (which are often nouns),
             * it made more sense to me to consider THM the default.
             */
        } else { // Case-scope
            (CaseScope.byGroup(groups[j].trimGlottal())?.toString(precision, ignoreDefault)
                ?: return error("Unknown case-scope : ${groups[j].trimGlottal()}")).plusSeparator(start = true) + (if (secondSegment.isEmpty() && precision > 0) "-THM" else "")
        } + secondSegment
        if (groups[j].startsWith("'")) { // Slot XI not filled, Slot X Aspect
            val vt = groups[j - 1]
            secondSegment = (Aspect.byT1(vt)?.toString(precision, ignoreDefault) ?: return error("Unknown aspect : $vt")).plusSeparator(start = true) + secondSegment
            j -= 2
        } else if (groups[j - 1].isVowel() && (groups[j - 2].isVowel() || groups[j - 1].contains("[wy]".toRegex()))) {
            /*
             * Parsing quirk nicely indicating that Slot XI is filled
             * If the parser encounters "awiu" for example, it'll interpret it as a sequence of two vowels, "awi" and "u".
             * Obviously this is not correct, but this is as far as I know the only occurence where this happens,
             * modular adjuncts set aside. Hence, this wrong analysis can be directly derived into a correct and precise
             * analysis.
             * Hacky.
             */
            val k = if (groups[j - 2].isVowel()) j - 2 else j - 1
            val slotTen = groups[k].substring(
                0,
                groups[k].indexOfFirst { it.toString().isConsonant() }
            )
            val slotEleven = groups[k].substring(
                groups[k].indexOfFirst { it.toString().isConsonant() },
                groups[k].length
            ) + if (k == j - 2) groups[j - 1] else ""
            /*
             * I know, the last few lines were very weird, but hey, I was decrypting the accidental mess
             */
            secondSegment = (parseSlotEleven(slotEleven, precision, ignoreDefault)
                ?: return error("Unknown phase/effect/level/aspect : $slotEleven")).plusSeparator(start = true) + secondSegment
            secondSegment = "-" + (parseVn(slotTen)?.toString(precision, ignoreDefault)
                ?: return error("Unknown context/valence : $slotTen")).plusSeparator(start = true) + secondSegment
            j = k - 1
        } else if (groups[j - 2] matches "'?[wy]".toRegex()) { // Slots X and XI filled, detected normally this time
            val hasGlottal = groups[j - 2].contains("'")
            val slotEleven = groups[j - 2].trimGlottal() + groups[j - 1]
            secondSegment = (parseSlotEleven(slotEleven, precision, ignoreDefault)
                ?: return error("Unknown phase/effect/level/aspect : $slotEleven")).plusSeparator(start = true) + secondSegment
            secondSegment = if (hasGlottal) {
                (Aspect.byT1(groups[j - 3])?.toString(precision, ignoreDefault)
                    ?: return error("Unknown aspect : ${groups[j - 3]}'")).plusSeparator(start = true)
            } else {
                (parseVn(groups[j - 3])?.toString(precision, ignoreDefault)
                    ?: return error("Unknown phase/effect/level/aspect : ${groups[j - 3]}")).plusSeparator(start = true)
            } + secondSegment
            j -= 4
        } else { // Slot XI not filled, Slot X context/valence
            secondSegment = (parseVn(groups[j - 1])?.toString(precision, ignoreDefault)
                ?: return error("Unknown phase/effect/level/aspect : ${groups[j - 1]}")).plusSeparator(start = true) + secondSegment
            j -= 2
        }
    }
    if (secondSegment.isEmpty() && stress == 0) { // Ensure that CNF/PPS be always marked
        secondSegment = "-CNF/PPS"
    }
    if (!contextMarked && (stress == 2 || stress == 3)) {
        secondSegment = "-FNC$secondSegment"
    }
    var delin = false
    // j is now either at Ca, or at the end of Slot IX
    if (i == j) { // We're at Ca, slots VII and IX are empty
        val c = groups[i].trimGlottal()
        val ca = parseCa(c.trimH())
        val alternate = if (stress == 0 || stress == 3) {
            Mood.values()
                .getOrNull(acceptableAlternateSlotEight.indexOf(c))
                ?.toString(precision)
        } else {
            CaseScope.values()
                .getOrNull(acceptableAlternateSlotEight.indexOf(c))
                ?.toString(precision)
        }
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
    } else if (groups[j].startsWith("'") || groups[j-2] matches "['h]".toRegex()) { // We're at Ca, slot IX is empty, but slot VII isn't
        if (groups[j-2] == "h") {
            delin = true
            acceptableAlternateSlotEight[0] = "ç"
        } else {
            acceptableAlternateSlotEight[0] = "h" // Slot XI is empty, Slot VII doesn't have delineation, so -ç- is not acceptable
        }
        val c = groups[j].trimGlottal()
        val ca = parseCa(c.trimH())
        val alternate = if (stress == 0 || stress == 3) {
            Mood.values()
                .getOrNull(acceptableAlternateSlotEight.indexOf(c))
                ?.toString(precision)
        } else {
            CaseScope.values()
                .getOrNull(acceptableAlternateSlotEight.indexOf(c))
                ?.toString(precision)
        }
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
        var caIndex = -1
        for (k in j downTo i) {
            if (groups[k] matches "['h]".toRegex()) { // End of slot VII, but the glottal stop isn't part of Ca
                if (groups[k].contains("h"))
                    delin = true
                caIndex = k+2
                break
            } else if (groups[k].startsWith("'") || groups[k].startsWith("h")) { // Ca reached
                caIndex = k
                break
            }
        }
        if (caIndex == -1) // We didn't encounter anything marking the end of slot VII, thus i was at Ca all along
            caIndex = i
        var unrecognized : String? = null
        val slotNine = groups.toList()
            .subList(caIndex+1, j+1)
            .chunked(2) {
                if (it.size == 1) {
                    unrecognized = "\u0000"
                    return@chunked null
                } else if (it[1].defaultForm() in INVALID_LEXICAL_CONSONANTS) {
                    unrecognized = "#${it[1]}"
                    return@chunked null
                }
                val a = parseAffix(it[1], it[0], false, precision)
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
                unrecognized!!.startsWith("#") -> error("'${unrecognized!!.substring(1)}' cannot be an affix consonant")
                unrecognized!!.startsWith("&") -> error("Unknown case vowel : ${unrecognized!!.substring(1)}")
                unrecognized!!.startsWith("^") -> error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${unrecognized!!.substring(1)}")
                else -> error("Unknown affix vowel : ${unrecognized!!.substring(1)}")
            }
        }
        if (slotNine.isNotEmpty())
            acceptableAlternateSlotEight[0] = "ç"
        secondSegment = "-$slotNine$secondSegment"
        val c = groups[caIndex].trimGlottal()
        val ca = parseCa(c.trimH())
        val alternate = if (stress == 0 || stress == 3) {
            Mood.values()
                .getOrNull(acceptableAlternateSlotEight.indexOf(c))
                ?.toString(precision)
        } else {
            CaseScope.values()
                .getOrNull(acceptableAlternateSlotEight.indexOf(c))
                ?.toString(precision)
        }
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
    while (k <= j) { // Reminder : affixes are CV rather than VC here
        val c = groups[k].trimH()
        var v : String
        if (k + 3 <= j && groups[k+2] matches "['h]".toRegex()) { // Vowel has standalone delineation (and potentially the end of slot VII but we don't care)
            v = if (groups[k+1] == groups[k+3]) {
                groups[k+1]
            } else {
                groups[k+1] + groups[k+3]
            }
            k += 2
        } else if (k + 2 == j) { // Something went wrong in vowel parsing, by now groups[k+2] should be a consonant
            return error("Affix group (slot XII) ended unexpectedly")
        } else { // Standard CV
            v = groups[k+1]
        }
        k += 2
        val aff = parseAffix(c, v, delin, precision)
        when {
            c.defaultForm() in INVALID_LEXICAL_CONSONANTS -> error("'${c}' cannot be an affix consonant")
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
    val stress = forceStress ?: groups.findStress()
    var i = 0
    var result = ""
    if (groups[0] matches "ç|h[lrwmn]".toRegex()) { // Initial Cc/Cm
        result += when {
            verbalFormative == null -> CaseScope.byGroup(groups[0])?.toString(precision, ignoreDefault)?.plusSeparator(sep = "|") + Mood.byGroup(groups[0])?.toString(precision, ignoreDefault)
            verbalFormative -> CaseScope.byGroup(groups[0])?.toString(precision, ignoreDefault)
            else -> Mood.byGroup(groups[0])?.toString(precision, ignoreDefault)
        }!!.plusSeparator()
        i++
    }
    if (groups[i].isVowel()) { // Vn or Vt
        result += if (groups.size == i+1 || !groups[i+1].startsWith("'")) { // Vn
            (parseVn(groups[i])?.toString(precision, ignoreDefault) ?: return error("Unknown valence/context : ${groups[i]}")).plusSeparator()
        } else { // Vt
            (Aspect.byT1(groups[i])?.toString(precision) ?: return error("Unknown aspect : ${groups[i]}")) + "-"
        }
        i++
    }
    if (i+1 < groups.size) {
        var slot = groups[i].trimGlottal() + groups[i+1]
        result += (parseSlotEleven(slot, precision, ignoreDefault)
                ?: Mood.byVowel(slot)?.toString(precision, ignoreDefault)
                ?: return error("Unknown phase/level/effect/aspect/mood : $slot"))
        i += 2
        if (i+1 < groups.size) { // Slot 4
            slot = groups[i].trimGlottal() + groups[i+1]
            result += if (groups[i] == "h") { // Mood
                (Mood.byVowel(slot)?.toString(precision, ignoreDefault) ?: return error("Unknown mood : $slot")).plusSeparator(start = true)
            } else {
                if (!groups[i].startsWith("'"))
                    return error("Couldn't parse slot IV of modular adjunct '${groups.joinToString("")}")
                (parseSlotEleven(slot, precision, ignoreDefault) ?: return error("Unknown phase/level/effect/aspect : $slot")).plusSeparator(start = true)
            }
        }
    }
    result += when (stress) {
        0 -> "-FML"
        -1, 1 -> ""
        2 -> "-CPT"
        3 -> "-FML+CPT"
        else -> throw IllegalStateException()
    }
    return result
}

fun parsePRA(groups: Array<String>, precision: Int, forceStress: Int? = null): String {
    val stress = forceStress ?: (groups.findStress() and 1) // Converting monosyllabic -1 into penultimate 1
    var result = ""
    when (groups.size) {
        2 -> {
            val ref = parseFullReferent(groups[0], precision) ?: return error("Unknown referent : ${groups[0]}")
            result += ref
            result += if (stress == 0) {
                "-" + (parseVk(groups[1])?.toString(precision)
                    ?: return error("Unknown illocution/sanction : ${groups[1]}"))
            } else {
                "/" + (Case.byVowel(groups[1])?.toString(precision)
                    ?: return error("Unknown case value : ${groups[1]}"))
            }
        }
        3 -> {
            val case2 =
                Case.byVowel(groups[0])?.toString(precision) ?: return error("Unknown case value : ${groups[0]}")
            when (groups[1].length) {
                1 -> {
                    val ref = parseFullReferent(groups[1], precision) ?: return error("Unknown referent : ${groups[1]}")
                    result += "$ref/"
                    result += if (stress == 0) {
                        "$case2-" + (parseVk(groups[2])?.toString(precision)
                                ?: return error("Unknown illocution/sanction : ${groups[2]}"))
                    } else {
                        (Case.byVowel(groups[2])?.toString(precision)
                                ?: return error("Unknown case value : ${groups[2]}")) + "/$case2"
                    }
                }
                2 -> {
                    val ref1 = parsePersonalReference(groups[1][1].toString())?.toString(precision) ?: return error("Unknown referent : ${groups[1][1]}")
                    val ref2 = parsePersonalReference(groups[1][0].toString())?.toString(precision) ?: return error("Unknown referent : ${groups[1][0]}")
                    result += "$ref2/$case2-$ref1"
                    result += if (stress == 0) {
                        "-" + (parseVk(groups[2])?.toString(precision) ?: return error("Unknown illocution/sanction : ${groups[2]}"))
                    } else {
                        "/" + (Case.byVowel(groups[2])?.toString(precision) ?: return error("Unknown case value : ${groups[2]}"))
                    }
                }
                else -> return error("Couldn't parse PRA : '${groups.joinToString("")}'")
            }
        }
        5 -> {
            val case1 =
                Case.byVowel(groups[0])?.toString(precision) ?: return error("Unknown case value : ${groups[0]}")
            val ref1 = parseFullReferent(groups[1], precision) ?: return error("Unknown referent : ${groups[1]}")
            result += "$ref1/$case1-"
            val ref2 = parseFullReferent(groups[3], precision) ?: return error("Unknown referent : ${groups[3]}")
            result += ref2
            result += if (stress == 0) {
                "-" + (parseVk(groups[4])?.toString(precision)
                    ?: return error("Unknown illocution/sanction : ${groups[4]}"))
            } else {
                "/" + (Case.byVowel(groups[4])?.toString(precision)
                    ?: return error("Unknown case value : ${groups[4]}"))
            }
        }
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
        result += (parseVv(groups[0])?.toString(precision, ignoreDefault) ?: return error("Unknown Vv value : ${groups[0]}")).plusSeparator()
        i++
    }
    result += (parseFullReferent(groups[i], precision) ?: return error("Unknown referent : ${groups[i]}")) + "/"
    result += (Case.byVowel(groups[i+1])?.toString(precision) ?: return error("Unknown case value : ${groups[i+1]}")) + "-"
    val specIndex = combinationPRASpecification.indexOf(groups[i+2]) // Cannot be -1 because of preemptive check
    result += Specification.values()[specIndex].toString(precision, ignoreDefault).plusSeparator()
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
                val a = parseAffix(it[1], it[0], false, precision)
                if (a.startsWith("#") || a.startsWith("@"))
                    unrecognized = a
                return@chunked a
            }
            .joinToString("-")
        if (unrecognized != null) {
            return if (unrecognized == "\u0000") {
                error("Affix group of combination PRA ended unexpectedly")
            } else if (unrecognized!!.startsWith("#")) {
                error("'${unrecognized!!.substring(1)}' cannot be an affix consonant")
            } else if (unrecognized!!.startsWith("&")) {
                error("Unknown case vowel : ${unrecognized!!.substring(1)}")
            } else if (unrecognized!!.startsWith("^")) {
                error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${unrecognized!!.substring(1)}")
            } else {
                error("Unknown affix vowel : ${unrecognized!!.substring(1)}")
            }
        }
        if (!result.endsWith("-"))
            result += "-"
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
            }
        }
    }
    return result
}

fun parseAffixualScoping(groups: Array<String>, precision: Int, ignoreDefault: Boolean, forceStress: Int? = null): String {
    val caFirst = groups[0] == "çw"
    var result = ""
    var i = 1
    while (i < groups.size) {
        var delin = false
        var c : String
        var v : String
        if (i + 1 >= groups.size) {
            if (result.contains("{") && groups[i] eq "a") { // Epenthetic a
                break
            } else if (result.isNotEmpty() && !result.contains("{")) { // Slots 4-5 not filled
                if (!ignoreDefault || caFirst || !("a" eq groups[i])) {
                    result = (scopeToString(caFirst, groups[i])
                            ?: return error("Unknown affixual scoping vowel : ${groups[i]}")) + result
                }
                break
            } else { // Final vowel without having parsed any affix
                return error("Affixual scoping adjunct ended unexpectedly")
            }
        } else if (groups[i+1] == "'") {
            v = if (i + 2 == groups.size) {
                return error("Affixual scoping adjunct ended unexpectedly")
            } else if (groups[i] eq groups[i+2]) {
                groups[i]
            } else {
                groups[i] + groups[i+2]
            }
            c = groups.getOrNull(i + 3) ?: return error("Affixual scoping adjunct ended unexpectedly")
            i += 2
            delin = true
        } else if (groups[i+1] in affixualScopingConsonants) {
            if (!ignoreDefault || caFirst || !("a" eq groups[i]) || !("w/y" eq groups[i+1])) {
                result = (scopeToString(caFirst, groups[i])
                        ?: return error("Unknown affixual scoping vowel : ${groups[i]}")) + result
                result += "-" + (scopeToString(!caFirst, groups[i+1])
                        ?: return error("Unknown affixual scoping consonant : ${groups[i+1]}"))
            }
            if (i + 2 == groups.size) {
                return error("Slot 4 of affixual scoping adjunct is filled, but no further suffixes are present")
            }
            i += 2
            continue
        } else if (groups[i+1].startsWith("'")) {
            v = groups[i]
            c = groups[i+1].substring(1)
            delin = true
        } else {
            v = groups[i]
            c = groups[i+1]
        }
        i += 2
        val aff = parseAffix(c, v, delin, precision)
        when {
            c.defaultForm() in INVALID_LEXICAL_CONSONANTS -> return error("'${c}' cannot be an affix consonant")
            aff.startsWith("@") -> return error("Unknown affix vowel : ${aff.substring(1)}")
            aff.startsWith("&") -> return error("Unknown case vowel : ${aff.substring(1)}")
            else -> result += "-$aff"
        }
    }
    if (result.startsWith("-")) {
        result = if (caFirst) {
            (scopeToString(true, "a") ?: throw IllegalStateException()) + result
        } else {
            result.drop(1)
        }
    }
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
    var i = 0
    var ca = false
    if (groups[0] == "h")
        i++
    val v : String
    val c : String
    if (groups[i+1] == "'") {
        if (i + 2 == groups.size) {
            return error("Affixual adjunct ended unexpectedly")
        } else if (groups[i] eq groups[i+2]) {
            ca = true
            v = groups[i]
            c = groups[i+3]
        } else {
            ca = true
            v = groups[i] + groups[i+2]
            c = groups[i+3]
        }
    } else if (groups[i+1].startsWith("'")) {
        v = groups[i]
        c = groups[i+1].substring(1)
    } else {
        v = groups[i]
        c = groups[i+1]
    }
    var stress = forceStress ?: groups.findStress()
    if (stress == -1) // Monosyllabic
        stress = 1 // I'll be consistent with 2011 Ithkuil, this precise behaviour is actually not documented
    val aff = parseAffix(c, v, stress == 0, precision)
    return when {
        c.defaultForm() in INVALID_LEXICAL_CONSONANTS -> error("'${c}' cannot be an affix consonant")
        aff.startsWith("@") -> error("Unknown affix vowel : ${aff.substring(1)}")
        aff.startsWith("&") -> error("Unknown case vowel : ${aff.substring(1)}")
        aff.startsWith("^") -> error("Unknown configuration/affiliation/extension/perspective/essence Ca cluster : ${aff.substring(1)}")
        else -> (if (ca) "{Ca}-" else if (ignoreDefault) "" else "{Stm}-") + aff
    }
}

fun error(s: String) = "\u0000" + s

fun errorList(s: String) = listOf("\u0000", s)
