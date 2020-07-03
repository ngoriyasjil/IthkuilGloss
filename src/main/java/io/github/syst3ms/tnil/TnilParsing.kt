package io.github.syst3ms.tnil

import net.dv8tion.jda.api.utils.MarkdownUtil
import java.io.File
import kotlin.streams.toList

val affixData: List<AffixData> by lazy {
    loadAffixes()
}
val rootData: List<RootData> by lazy {
    loadRoots()
}

fun String.isVowel() = this.defaultForm() in flatVowelForm

fun String.isConsonant() = this.all { it.toString().defaultForm() in CONSONANTS }

fun String.isModular() = this matches "'?([wy]|h.*)".toRegex()

fun String.hasStress() = this.isVowel() && this.defaultForm() != this

fun String.trimGlottal() = this.replace("'", "")

fun String.trimH() = this.replace("^('?)h".toRegex(), "$1")

fun String.plusSeparator(start: Boolean = false, sep: String = "-") = when {
    this.isEmpty() -> this
    start -> "$sep$this"
    else -> "$this$sep"
}

fun List<Precision>.toString(precision: Int, ignoreDefault: Boolean = false, stemUsed : Boolean = false, designationUsed : Boolean = false) = this
        .map {
            when {
                it is Stem && stemUsed || it is Designation && designationUsed -> {
                    val s = it.toString(precision, ignoreDefault)
                    when {
                        s.isEmpty() -> ""
                        else -> MarkdownUtil.underline(s)
                    }
                }
                else -> it.toString(precision, ignoreDefault)
            }
        }
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
        .replace("[úù]".toRegex(), "u")
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
            listOf(
                    it.replace("[ìı]".toRegex(), "i")
                      .replace("ù", "u")
            )
        }
    }.reversed()
        .indexOfFirst { it.hasStress() }
    return when {
        this.count(String::isVowel) == 1 -> -1
        i == -1 -> 1
        else -> i / 2
    }
}

fun String.splitGroups(): Array<String> {
    val groups = arrayListOf<String>()
    var chars = toCharArray()
        .map(Char::toString)
        .toList()
    while (chars.isNotEmpty()) {
        val group = if (chars[0].isVowel()) {
            if (chars.getOrNull(1)?.isVowel() == true) {
                chars[0] + chars[1]
            } else {
                chars[0]
            }
        } else if (!chars[0].isConsonant()) {
            throw IllegalArgumentException("Non-Ithkuil character : ${chars[0]}")
        } else {
            chars.takeWhile(String::isConsonant).joinToString("")
        }
        chars = chars.subList(group.length, chars.size)
        groups += group
    }
    return groups.toTypedArray()
}

fun scopeToString(letter: String, ignoreDefault: Boolean) = if (letter == "a" && ignoreDefault) {
    ""
} else when (letter) {
    "a" -> "{StmDom}"
    "u" -> "{StmSub}"
    "e" -> "{CaDom}"
    "i" -> "{CaSub}"
    "o" -> "{Form}"
    "ö" -> "{All}"
    else -> null
}

fun parseFullReferent(s: String, precision: Int, ignoreDefault: Boolean, final: Boolean = false): String? {
    val singleRef = parsePersonalReference(s)
    if (singleRef != null) {
        return singleRef.toString(precision, ignoreDefault)
    }
    val singleJoined = s.toCharArray().map { parsePersonalReference(it.toString(), final) }
    if (singleJoined.none { it == null }) {
        return singleJoined.requireNoNulls()
            .sortedBy { (it[0] as Enum<*>).ordinal }
            .joinToString("+", "[", "]") { it.toString(precision, ignoreDefault) }
    } else if (s.length == 3) {
        if (s.endsWith("ç") || s.endsWith("h") || s.endsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s[0].toString(), final) to parsePersonalReference(s.substring(1, 3), final)
            return if (ref1 != null && ref2 != null) {
                "[" + ref1.toString(precision, ignoreDefault) + "/" + ref2.toString(precision, ignoreDefault) + "]"
            } else {
                null
            }
        } else if (s[1] == 'ç' || s[1] == 'h' || s.startsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s.substring(0, 2), final) to parsePersonalReference(s[2].toString(), final)
            return if (ref1 != null && ref2 != null) {
                "[" + ref1.toString(precision, ignoreDefault) + "/" + ref2.toString(precision, ignoreDefault) + "]"
            } else {
                null
            }
        }
    } else if (s.length == 4) {
        val halves = s.chunked(2)
            .map { r -> parsePersonalReference(r, final) }
        return if (halves.none { it == null }) {
            halves.requireNoNulls()
                .sortedBy { (it[0] as Enum<*>).ordinal }
                .joinToString("+", "[", "]") { it.toString(precision, ignoreDefault) }
        } else {
            null
        }
    }
    return null
}

data class AffixData(val cs: String, val abbr: String, val desc: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AffixData
        if (cs != other.cs) return false
        if (abbr != other.abbr) return false
        if (!desc.contentEquals(other.desc)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cs.hashCode()
        result = 31 * result + abbr.hashCode()
        result = 31 * result + desc.contentHashCode()
        return result
    }
}

fun loadAffixes(): List<AffixData> {
    val file = File("./affixes.txt")
    val reader = file.bufferedReader()
    return reader.lines()
            .filter { it.isNotBlank() }
            .map { it.split("|") }
            .map { AffixData(it[0], it[1], it.drop(2).toTypedArray()) }
            .toList()
}

fun parseAffix(c: String, v: String, precision: Int): String {
    val vi = affixVowel.indexOfFirst { it eq v }
    if (vi == -1 && !(v eq "eä" || v eq "öä")) {
        return "@$v"
    }
    val deg = vi % 10
    var delin = false
    val type = if (vi < 30) {
        vi / 10 + 1
    } else {
        delin = true
        vi / 10 - 2
    }
    val aff = affixData.find { it.cs == c }
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
            "($case\\inverse accessor-" + (if (c == "lw") "Type 1" else "Type 2") + (if (delin) "(delineation)" else "") + ")"
        } else {
            "(${case}ia-" + (if (c == "lw") "T1" else "T2") + (if (delin) "d" else "") + ")"
        }
    } else if (v eq "eä" || v eq "öä") {
        val ca = parseCa(c) ?: return "^$c"
        return "(" + (if (v eq "öä") {
            if (precision > 0) "delineation-" else "d-"
        } else "") + "${ca.toString(precision)})"
    }
    // Special cases
    return if (aff != null) {
        if (precision > 0 || aff.desc.size == 9 && deg == 0) {
            when (aff.desc.size) {
                1 -> "'" + aff.desc[0] + "'" + (0x2080 + type).toChar() + (if (delin) "(delineation)" else "") + "/" + deg
                9 -> "'" + aff.desc[deg - 1] + "'" + (0x2080 + type).toChar() + if (delin) "(delineation)" else ""
                else -> throw IllegalArgumentException("Invalid number of affix degrees")
            }
        } else {
            aff.abbr + (0x2080 + type).toChar() + (if (delin) "d" else "") + "/" + deg
        }
    } else {
        MarkdownUtil.bold(c.defaultForm()) + (0x2080 + type).toChar() + (if (delin) "d" else "") + "/" + deg
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

fun parseRoot(c: String, precision: Int, stem: Int = 0, formal: Boolean = false) : Triple<String, Boolean, Boolean> {
    val root = rootData.find { it.cr == c } ?: return Triple(MarkdownUtil.bold(c.defaultForm()), second = false, third = false)
    if (precision > 0) {
        var stemUsed = false
        var designationUsed = false
        val d = when (root.dsc.size) {
            1 -> root.dsc[0] // Only basic description, no precise stem description
            4 -> {
                stemUsed = true
                root.dsc[stem] // basic description + IFL Stems 1,2,3
            }
            7 -> { // basic description + IFL & FML Stems 1,2,3
                stemUsed = true
                designationUsed = true
                when {
                    stem == 0 -> root.dsc[0]
                    formal -> root.dsc[stem+3]
                    else -> root.dsc[stem]
                }
            }
            9 -> { // basic description + IFL & FML Stems 0,1,2,3 ; only used for the carrier root
                stemUsed = true
                designationUsed = true
                root.dsc[stem + if (formal) 5 else 1]
            }
            else -> throw IllegalArgumentException("Root format is invalid : found ${root.dsc.size} arguments in the description of root -${root.cr}-")
        }.toLowerCase()
        return Triple("'$d'", stemUsed, designationUsed)
    } else {
        return Triple("'${root.dsc[0].toLowerCase()}'", second = false, third = false)
    }
}
