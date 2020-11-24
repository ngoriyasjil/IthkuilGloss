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

    val ss = s.replace("^çë?".toRegex(), "")

    val groups = ss.splitGroups()
    if (groups.isEmpty()) {
        return error("Empty word")
    }

    val ssgloss = when (precision) {
        0 -> "[.]-"
        1 -> "[sentence:]-"
        2, 3, 4 -> "[sentence start]-"
        else -> ""
    }

    return (if (s != ss) ssgloss else "") + when {
        groups.size == 1 && groups[0].isConsonant() ->  {
            Bias.byGroup(groups[0])?.toString(precision) ?: error("Unknown bias: ${groups[0]}")
        }
        groups[0] in setOf("hl", "hm", "hn", "hr") && (groups.size == 2) -> {
            val v = groups[1]
            parseSuppletiveAdjuncts(groups[0], v, precision, ignoreDefault) ?: error("Unknown carrier adjunct: $s")
        }
        groups[0] == "h" && groups.size == 2 -> {
            val (register, initial) = Register.byVowel(groups.last()) ?: return error("Unknown register adjunct: $s")
            return "<" + (if (initial) "" else "/") + register.toString(precision, ignoreDefault) + ">"
        }
        groups.size == 2 && groups[0].isConsonant() && !groups[0].isModular()
                || groups.size >= 4 && !groups[0].isModular() && (groups[1] == "ë" || groups[2] matches "[wy]".toRegex()) -> {
            parsePRA(groups, precision, ignoreDefault)
        }
        groups.size >= 4 && groups[0].isVowel() && groups[3] in COMBINATION_PRA_SPECIFICATION
                || groups.size >= 3 && groups[0] !in CC_CONSONANTS && groups[2] in COMBINATION_PRA_SPECIFICATION -> {
            parseCombinationPRA(groups, precision, ignoreDefault)
        }
        groups.size in 2..3 && groups[1].isConsonant() && !groups[1].isModular()
                || groups.size in 4..5 && groups[1] == "y" && !groups[3].isModular() -> {
            parseAffixual(groups, precision, ignoreDefault)
        }
        groups.size >= 5 && groups[0].isConsonant() && groups[2].removePrefix("'") in CC_CONSONANTS
                || (groups[0] == "ë") && (groups[3].removePrefix("'") in CC_CONSONANTS) -> {
            parseAffixualScoping(groups, precision, ignoreDefault)
        }

        groups.all { it.isVowel() || it.isModular() } -> {
            parseModular(groups, precision, ignoreDefault)
        }
        else -> parseFormative(groups, precision, ignoreDefault)
    }
}

@Suppress("UNCHECKED_CAST")
fun parseFormative(groups: Array<String>, precision: Int, ignoreDefault: Boolean) : String {

    val stress = groups.findStress().coerceAtLeast(0)
    var index = 0

    val (concatenation, shortcut) = if (groups[0] in CC_CONSONANTS) {
        index++
        parseCc(groups[0])
    } else Pair(null, null)

    val relation = if (concatenation != null) {
        when (stress){
            2 -> Relation.FRAMED
            else -> Relation.UNFRAMED
        }
    } else null

    val vv = if (index == 0 && groups[0].isConsonant()) "a" else {
        groups[index].also { index++ }
    }

    val slotII = parseVv(vv, shortcut) ?: return error("Unknown Vv value: $vv")

    val stem = (slotII[0] as Stem).ordinal
    val (root, stemUsed) = parseRoot(groups[index], precision, stem)
    index++

    val vr = if (shortcut != null) "a" else {
        groups[index].also { index++ }
    }

    val slotIV = parseVr(vr) ?: return error("Unknown Vr value: $vr")

    val csVxAffixes : MutableList<Affix> = mutableListOf()

    if (shortcut == null) {
        var indexV = index
        while (true) {
            if (indexV+1 >= groups.size || groups[indexV] in CN_CONSONANTS || groups[indexV] == "-") {
                csVxAffixes.clear()
                indexV = index
                break
            } else if (groups[indexV].isGlottalCa()) {
                break
            }

            val (vx, glottal) = unGlottalVowel(groups[indexV+1]) ?: return error("Unknown vowelform: ${groups[indexV+1]}")

            csVxAffixes.add(Affix(vx, groups[indexV]))
            indexV += 2

            if (glottal) break
        }
        index = indexV

    }

    if (csVxAffixes.size == 1) csVxAffixes[0].canBePraShortcut = true


    var cnInVI = false

    val slotVI = if (shortcut == null) {
        val ca = if (groups.getOrNull(index)?.isGlottalCa()
                        ?: return error("Formative ended unexpectedly")) {
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

            val (vx, glottal) = unGlottalVowel(groups[index+1]) ?: return error("Unknown vowelform: ${groups[index+1]}")

            vxCsAffixes.add(Affix(groups[index], vx))
            index += 2

            if (glottal) {
                vxCsAffixes.add(PrecisionString("{end of slot V}", "{Ca}"))
            }
        }
    }

    if (vxCsAffixes.size == 1) (vxCsAffixes[0] as? Affix)?.canBePraShortcut = true

    val marksMood = (stress == 0)

    val slotVIII: List<Precision>? = when {
        cnInVI -> {
            parseVnCn("a", groups[index], marksMood).also { index++ } ?: return error("Unknown Cn value in Ca: ${groups[index]}")
        }
        groups.getOrNull(index+1) in CN_CONSONANTS -> {
            parseVnCn(groups[index], groups[index+1], marksMood).also { index += 2 } ?: return error("Unknown VnCn value: ${groups[index] + groups[index+1]}")
        }
        else -> null
    }


    val vcVk = groups.getOrNull(index) ?: "a"

    val slotIX = if (concatenation == null) {
        when (stress) {
            0 -> parseVk(vcVk) ?: return error("Unknown Vk form $vcVk")
            1, 2 -> listOf(Case.byVowel(vcVk) ?: return error("Unknown Vc form $vcVk"))
            else -> return error("Unknown stress: $stress from ultimate")
        }
    } else {
        when (stress) {
            1 -> listOf(Case.byVowel(vcVk) ?: return error("Unknown Vf form $vcVk (penultimate stress)"))
            0 -> {
                val glottalified = when (vcVk.length) {
                    1 -> "$vcVk'$vcVk"
                    2 -> "${vcVk[0]}'${vcVk[1]}"
                    else -> return error("Vf form is too long: $vcVk")
                }
                listOf(Case.byVowel(glottalified) ?: return error("Unknown Vf form $vcVk (ultimate stress)"))
            }
            else -> return error("Unknown stress for concatenated formative: $stress from ultimate")
        }
    }
    index++

    val cyMarksMood = (stress == 0) || (stress == 2 && slotVIII != null)

    val slotX = if (concatenation == null && index < groups.size) {
        parseCbCy(groups[index], cyMarksMood)
    } else null

    val parentFormative = if (groups.getOrNull(index) == "-") {
        if (concatenation != null) {
            parseFormative(groups.drop(index+1).toTypedArray(), precision, ignoreDefault)
        } else return error("Non-concatenated formative hyphenated")

    } else null

    val slotList: List<Any> = listOfNotNull(relation, concatenation, slotII, PrecisionString(root), slotIV) +
            csVxAffixes + listOfNotNull(slotVI) + vxCsAffixes + listOfNotNull(slotVIII, slotIX, slotX)

    val parsedFormative : String = slotList.map {
        if (it is List<*>) {
            (it as List<Precision>).toString(precision, ignoreDefault, stemUsed = stemUsed) // Wacky casting, beware.
        } else (it as Precision).toString(precision, ignoreDefault)
    }.filter { it.isNotEmpty() }.joinToString(SLOT_SEPARATOR)

    return if (parentFormative != null) {
        "$parsedFormative $parentFormative"
    } else parsedFormative

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
            result += (parseFullReferent(groups[i], precision, ignoreDefault)
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
    val scope = affixAdjunctScope((groups.getOrNull(i+2)?.defaultForm()), ignoreDefault)
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
