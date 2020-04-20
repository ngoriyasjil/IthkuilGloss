package io.github.syst3ms.tnil

import net.dv8tion.jda.api.utils.MarkdownUtil
import java.io.File
import java.lang.IllegalArgumentException
import kotlin.streams.toList

val affixData: List<AffixData> by lazy {
    loadAffixes()
}
val rootData: List<RootData> by lazy {
    loadRoots()
}

fun String.isVowel() = VOWEL_FORM.any { it eq this }

fun String.isConsonant() = this.toCharArray().all { it.toString().defaultForm() in CONSONANTS }

fun String.hasStress() = VOWEL_FORM.flatMap { it.split("/") }
    .any { it eq this && it != this }

fun String.trimGlottal() = this.replace("'", "")

fun String.trimH() = this.replace("^('?)h".toRegex(), "$1")

fun String.plusSeparator(start: Boolean = false, sep: String = "-") = when {
    this.isEmpty() -> this
    start -> "$sep$this"
    else -> "$this$sep"
}

fun List<Precision>.toString(precision: Int, ignoreDefault: Boolean = false) = this
        .map { it.toString(precision, ignoreDefault) }
        .filter { it.isNotEmpty() }
        .joinToString("/")

infix fun String.eq(s: String): Boolean = if ("/" in this) {
    this.split("/").any { it eq s }
} else {
    this.defaultForm() == s.defaultForm()
}

fun String.defaultForm() = this.replace("á", "a")
        .replace("â", "ä")
        .replace("é", "e")
        .replace("ê", "ë")
        .replace("[ìíı]".toRegex(), "i")
        .replace("ó", "o")
        .replace("ô", "ö")
        .replace("ú", "u")
        .replace("û", "ü")
        .replace("[ṭŧ]".toRegex(), "ţ")
        .replace("[ḍđ]".toRegex(), "ḑ")
        .replace("[łḷ]".toRegex(), "ļ")
        .replace("ż", "ẓ")
        .replace("ṇ", "ň")
        .replace("ṛ", "ř")

fun Array<String>.findStress(): Int {
    val i = this.flatMap {
        if (it.isVowel() && it.length == 3) {
            it.toCharArray().map(Char::toString)
        } else {
            listOf(it.replace("[ìı]".toRegex(), "i"))
        }
    }.reversed()
        .indexOfFirst { it.hasStress() }
    return when {
        this.count(String::isVowel) == 1 -> -1
        i == -1 -> 1
        else -> i / 2
    }
}

fun String.splitGroups(noSemis: Boolean = false): Array<String> {
    val groups = arrayListOf<String>()
    var chars = toCharArray()
        .map(Char::toString)
        .toList()
    while (chars.isNotEmpty()) {
        var group: String? = null
        if (chars[0].isVowel()) {
            for (i in (if (noSemis) 2 else 3).coerceAtMost(chars.size) downTo 1) {
                val potentialVowel = chars.subList(0, i).joinToString("")
                if (potentialVowel.isVowel()) {
                    group = potentialVowel
                    break
                }
            }
        } else {
            group = chars.takeWhile(String::isConsonant).joinToString("")
        }
        chars = chars.subList(group!!.length, chars.size)
        groups += group
    }
    return groups.toTypedArray()
}

fun scopeToString(ca: Boolean, letter: String): String? {
    return "{" +
            (if (ca) "Ca" else "Stm") +
            when {
                "a" eq letter || "w/y" eq letter -> "Slot"
                "e" eq letter || "h" eq letter -> "Sub"
                "o" eq letter || "'w/'y" eq letter -> "Form"
                "i/u" eq letter || "'h" eq letter -> "All"
                else -> return null
            } +
            "}"
}

fun parseFullReferent(s: String, precision: Int): String? {
    val singleRef = parsePersonalReference(s)
    if (singleRef != null) {
        return singleRef.toString(precision)
    }
    val singleJoined = s.toCharArray().map { parsePersonalReference(it.toString()) }
    if (singleJoined.none { it == null }) {
        return singleJoined.requireNoNulls()
            .sortedBy { (it[0] as Enum<*>).ordinal }
            .joinToString("+", "[", "]") { it.toString(precision) }
    } else if (s.length == 3) {
        if (s.endsWith("ç") || s.endsWith("h") || s.endsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s[0].toString()) to parsePersonalReference(s.substring(1, 3))
            return if (ref1 != null && ref2 != null) {
                "[" + ref1.toString(precision) + "/" + ref2.toString(precision) + "]"
            } else {
                null
            }
        } else if (s[1] == 'ç' || s[1] == 'h' || s.startsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s.substring(0, 2)) to parsePersonalReference(s[2].toString())
            return if (ref1 != null && ref2 != null) {
                "[" + ref1.toString(precision) + "/" + ref2.toString(precision) + "]"
            } else {
                null
            }
        }
    } else if (s.length == 4) {
        val halves = s.chunked(2)
            .map(::parsePersonalReference)
        return if (halves.none { it == null }) {
            halves.requireNoNulls()
                .sortedBy { (it[0] as Enum<*>).ordinal }
                .joinToString("+", "[", "]") { it.toString(precision) }
        } else {
            null
        }
    }
    return null
}

data class AffixData(val cs: String, val abbr: String, val desc: Array<String>)

fun loadAffixes(): List<AffixData> {
    val file = File("./affixes.txt")
    val reader = file.bufferedReader()
    return reader.lines()
            .filter { it.isNotBlank() }
            .map { it.split("|") }
            .map { AffixData(it[0], it[1], it.drop(2).toTypedArray()) }
            .toList()
}

fun parseAffix(c: String, v: String, delin: Boolean, precision: Int): String {
    // Special cases
    if (c == "rl") { // case-stacking affix
        val case = Case.byVowel(v)?.toString(precision) ?: return "&$v"
        return "($case" + (if (delin && precision > 0) "(delineation)" else if (delin) "d" else "") + ")"
    } else if (c == "ll" || c == "rr") { // case-accessor affix
        val case = Case.byVowel(v)?.toString(precision) ?: return "&$v"
        return if (precision > 0) {
            "($case\\accessor-" + (if (c == "ll") "Type 1" else "Type 2") + (if (delin) "(delineation)" else "") + ")"
        } else {
            "(${case}a-" + (if (c == "ll") "T1" else "T2") + (if (delin) "d" else "") + ")"
        }
    } else if (c == "lw" || c == "ly") { // inverse case-accessor affix
        val case = Case.byVowel(v)?.toString(precision) ?: return "&$v"
        return if (precision > 0) {
            "($case\\inverse accessor-" + (if (c == "ll") "Type 1" else "Type 2") + (if (delin) "(delineation)" else "") + ")"
        } else {
            "(${case}ia-" + (if (c == "lw") "T1" else "T2") + (if (delin) "d" else "") + ")"
        }
    } else if (v eq "eo" || v eq "oe") {
        val ca = parseCa(c) ?: return "^$c"
        return "(" + (if (v eq "oe") {
            if (precision > 0) "delineation-" else "d-"
        } else "") + "$ca)"
    }
    val vi = affixVowel.indexOfFirst { it eq v }
    if (vi == -1) {
        return "@$v"
    }
    val deg = vi % 10
    val type = vi / 10 + 1
    val aff = affixData.find { it.cs == c }
    return if (aff != null) {
        if (precision > 0) {
            when (aff.desc.size) {
                1 -> "'" + aff.desc[0] + "'" + (0x2080 + type).toChar() + (if (delin) "(delineation)" else "") + "/" + deg
                9 -> "'" + aff.desc[deg] + "'" + (0x2080 + type).toChar() + if (delin) "(delineation)" else ""
                else -> throw IllegalArgumentException("Invalid number of affix degrees")
            }
        } else {
            aff.abbr + (0x2080 + type).toChar() + (if (delin) "d" else "") + "/" + deg
        }
    } else {
        MarkdownUtil.underline(c.defaultForm()) + (0x2080 + type).toChar() + (if (delin) "d" else "") + "/" + deg
    }
}

data class RootData(val cr: String, val dsc: List<String>)

fun loadRoots(): List<RootData> {
    val file = File("./roots.txt")
    val reader = file.bufferedReader()
    return reader.lines()
        .map { it.split("|") }
        .map { RootData(it[0], it.subList(1, it.size)) }
        .toList()
}

fun parseRoot(c: String, precision: Int, stem: Int = 0, formal: Boolean = false) : String {
    val root = rootData.find { it.cr == c } ?: return MarkdownUtil.underline(c.defaultForm())
    if (precision > 0) {
        val d = when (root.dsc.size) {
            1 -> root.dsc[0] // Only basic description, no precise stem description
            4 -> root.dsc[stem] // basic description + IFL Stems 1,2,3
            7 -> { // basic description + IFL & FML Stems 1,2,3
                when {
                    stem == 0 -> root.dsc[0]
                    formal -> root.dsc[stem+3]
                    else -> root.dsc[stem]
                }
            }
            8 -> root.dsc[stem + if (formal) 4 else 0] // IFL & FML Stems 0,1,2,3
            else -> throw IllegalArgumentException("Root format is invalid : found ${root.dsc.size} arguments in the description of root -${root.cr}-")
        }.toLowerCase()
        return "'$d'"
    } else {
        return "'${root.dsc[0].toLowerCase()}'"
    }
}
