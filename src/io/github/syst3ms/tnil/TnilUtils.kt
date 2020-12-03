package io.github.syst3ms.tnil

import net.dv8tion.jda.api.utils.MarkdownUtil

var affixData: List<AffixData> = emptyList()
var rootData: List<RootData> = emptyList()

fun error(s: String) = "\u0000" + s

fun errorList(s: String) = listOf("\u0000", s)

val VOWELS = setOf("a", "ä", "e", "ë", "i", "ö", "o", "ü", "u")

fun String.isVowel() = when (length) {
    1, 2 -> all { it.toString().defaultForm() in VOWELS }
    3 -> this[1] == '\'' && this[0].toString().defaultForm() in VOWELS && this[2].toString().defaultForm() in VOWELS
    else -> false
}

fun String.isConsonant() = this.all { it.toString().defaultForm() in CONSONANTS }

fun String.isModular() = this matches "'?([wy]|h(?:lw)?.*)".toRegex()

fun String.hasStress() = this.isVowel() && this.defaultForm() != this //Dangerous


fun String.withZeroWidthSpaces() = this.replace("([/—-])".toRegex(), "\u200b$1")


//Deals with series three vowels and non-default consonant forms
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
    val i = this.filter(String::isVowel)
            .map { it.replace("[ìı]".toRegex(), "i").replace("ù", "u") }
            .flatMap {
                val (series, form) = seriesAndForm(it.defaultForm())
                if (it.length == 2 && series != 2 && !(series == 3 && form == 5)) {
                    it.toCharArray().map(Char::toString)
                } else {
                    listOf(it)
                }
            }
            .reversed()
            .indexOfFirst { it.hasStress() }
    return when {
        this.count(String::isVowel) == 1 -> -1
        i == -1 -> 1
        else -> i
    }
}

fun String.splitGroups(): Array<String> {
    val groups = arrayListOf<String>()
    var chars = toCharArray()
            .map(Char::toString)
            .toList()
    while (chars.isNotEmpty()) {
        val group = when {
            chars[0].defaultForm().isVowel() -> {
                if (chars.size >= 3 && (chars[0] + chars[1] + chars[2]).isVowel()) {
                    chars[0] + chars[1] + chars[2]
                } else if (chars.size >= 2 && (chars[0] + chars[1]).isVowel()) {
                    chars[0] + chars[1]
                } else {
                    chars[0]
                }
            }
            chars[0].isConsonant() -> {
                chars.takeWhile(String::isConsonant).joinToString("")
            }

            chars[0] == "-" -> chars[0]

            else -> {
                throw IllegalArgumentException("Non-Ithkuil character: ${chars[0]}")
            }
        }
        chars = chars.subList(group.length, chars.size)
        groups += group
    }
    return groups.toTypedArray()
}

val BICONSONANTAL_PRS = setOf("th", "ph", "kh", "ll", "rr", "řř")

fun parseFullReferent(s: String, precision: Int, ignoreDefault: Boolean): String? {
    val refList = mutableListOf<Slot>()
    var index = 0

    while (index < s.length) {
        refList.add(
                if (index + 1 < s.length && s.substring(index, index + 2) in BICONSONANTAL_PRS) {
                    parsePersonalReference(s.substring(index, index + 2)).also { index += 2 }
                            ?: return null
                } else parsePersonalReference(s.substring(index, index + 1)).also { index++ }
                        ?: return null
        )
    }
    return when (refList.size) {
        0 -> null
        1 -> refList[0].toString(precision, ignoreDefault)
        else -> refList
                .joinToString(REFERENT_SEPARATOR, REFERENT_START, REFERENT_END)
                { it.toString(precision, ignoreDefault) }
    }
}


fun parseAffixes(data: String): List<AffixData> = data
        .lines()
        .asSequence()
        .drop(1)
        .map    { it.split("\t") }
        .filter { it.size >= 11 }
        .map    { AffixData(it[0], it[1], it.subList(2, 11).toTypedArray()) }
        .toList()


val CASE_AFFIXES = setOf("ll", "lw", "sw", "zw", "šw", "rr", "ly", "sy", "zy", "šy")

fun parseAffix(cs: String, vx: String,
               precision: Int,
               ignoreDefault: Boolean,
               canBePraShortcut: Boolean = false,
               noType: Boolean = false) : String {
    if (vx == CA_STACKING_VOWEL) {
        val ca = parseCa(cs)?.toString(precision, ignoreDefault) ?: return "(Unknown Ca)"

        return if (ca.isNotEmpty()) {
            "($ca)"
        } else {
            "(${Configuration.UNIPLEX.toString(precision, ignoreDefault = false)})"
        }
    }

    if (cs in CASE_AFFIXES) {
        val vc = when (cs) {
            "ll", "lw", "sw", "zw", "šw" -> vx
            "rr", "ly", "sy", "zy", "šy" -> glottalVowel(vx)?.first ?: return "(Unknown vowel: $vx)"
            else -> return "(Unknown case affix form)"
        }

        val s = if (precision > 1) when (cs) {
            "ll", "rr", "lw", "ly" -> "case accessor:"
            "sw", "sy", "zw", "zy" -> "inverse accessor:"
            "šw", "šy" -> "case-stacking:"
            else -> return "(Unknown case affix form)"
        } else when (cs) {
            "ll", "rr", "lw", "ly" -> "acc:"
            "sw", "sy", "zw", "zy" -> "ia:"
            "šw", "šy" -> ""
            else -> return "(Unknown case affix form)"
        }

        val type = when (cs) {
            "ll", "rr", "sw", "sy" -> "\u2081"
            "lw", "ly", "zw", "zy" -> "\u2082"
            else -> ""
        }

        val case = Case.byVowel(vc)?.toString(precision) ?: return "(Unknown case: $vc)"
        return "($s$case)$type"

    }

    var (type, degree) = seriesAndForm(vx)

    if (canBePraShortcut && type == 3) {
        return parsePraShortcut(cs, vx, precision) ?: "(Unknown PRA shortcut)"
    }

    if (type == -1 && degree == -1) {
       degree = 0
       type = when (vx) {
           "üa" -> 1
           "üe" -> 2
           "üo" -> 3
           else -> return "(Unknown Vx: $vx)"
       }
    }

    val aff = affixData.find { it.cs == cs }

    val affString = when {
        aff == null -> "**$cs**/$degree"
        precision == 0 || degree == 0 -> "${aff.abbr}/$degree"
        precision > 0 -> "'${aff.desc.getOrNull(degree-1) ?: return "(Unknown affix degree: $degree)"}'"
        else -> return "(Unknown affix: $cs)"
    }

    val t = if (!noType) when (type) {
        1 -> "\u2081"
        2 -> "\u2082"
        3 -> "\u2083"
        else -> return "(Unknown type)"
    } else ""

    return "$affString$t"

}

fun parseRoots(data: String): List<RootData> = data
        .lines()
        .asSequence()
        .drop(1)
        .map    { it.split("\t") }
        .filter { it.size >= 5 }
        .map    { RootData(it[0], it.subList(1, 5)) }
        .toList()

fun parseRoot(c: String, precision: Int, stem: Int = 0): Pair<String, Boolean> {
    val root = rootData.find { it.cr == c.defaultForm() } ?: return MarkdownUtil.bold(c.defaultForm()) to false
    return if (precision > 0) {
        var stemUsed = false
        val d = when (val stemDsc = root.dsc[stem]) {
            "" -> root.dsc[0]
            else -> {
                stemUsed = true
                stemDsc
            }
        }

        "'$d'" to stemUsed
    } else {
        "'${root.dsc[0]}'" to false
    }
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

data class RootData(val cr: String, val dsc: List<String>)

class SentenceParsingState(var carrier: Boolean = false,
                           var register: MutableList<Register> = mutableListOf(),
                           var quotativeAdjunct: Boolean = false,
                           var concatenative: Boolean = false,
                           stress: Int? = null,
                           var isLastFormativeVerbal : Boolean? = null,
                           var rtiAffixScope: String? = null) {
    var forcedStress : Int? = stress
        get() {
            val old = field
            forcedStress = null
            return old
        }
}
