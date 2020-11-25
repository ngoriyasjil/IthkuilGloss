package io.github.syst3ms.tnil

import net.dv8tion.jda.api.utils.MarkdownUtil

var affixData: List<AffixData> = emptyList()
var rootData: List<RootData> = emptyList()

fun String.isVowel() = this.defaultForm() in flatVowelForm

fun String.isConsonant() = this.all { it.toString().defaultForm() in CONSONANTS }

fun String.isModular() = this matches "'?([wy]|h(?:lw)?.*)".toRegex()

fun String.hasStress() = this.isVowel() && this.defaultForm() != this

fun String.isInvalidLexical() = this.defaultForm() in INVALID_LEXICAL_CONSONANTS || this.startsWith("h") || this.contains("'")

fun String.trimGlottal() = if (isGlottalCa()) this.drop(1) else this

fun String.plusSeparator(start: Boolean = false, sep: String = SLOT_SEPARATOR) = when {
    this.isEmpty() -> this
    start -> "$sep$this"
    else -> "$this$sep"
}

fun String.withZeroWidthSpaces() = this.replace("([/—-])".toRegex(), "\u200b$1")

fun List<Precision>.toString(precision: Int, ignoreDefault: Boolean = false, stemUsed: Boolean = false) = join(
        *this.map {
            when {
                it is Stem && stemUsed -> {
                    val s = it.toString(precision, ignoreDefault)
                    when {
                        s.isEmpty() -> ""
                        else -> MarkdownUtil.underline(s)
                    }
                }
                else -> it.toString(precision, ignoreDefault)
            }
        }.toTypedArray()
)

fun join(vararg strings: String, sep: String = CATEGORY_SEPARATOR) = strings.filter { it.isNotEmpty() }
        .joinToString(sep)


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
                if (it.length == 2 && flatVowelForm.indexOf(it.defaultForm()) !in 9 until 18 && it.defaultForm() != "ëu") {
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
            chars[0].isVowel() -> {
                if (chars.getOrNull(1) == "'" && chars.getOrNull(2)?.isVowel() == true) {
                    chars[0] + chars[1] + chars[2]
                } else if (chars.getOrNull(1)?.isVowel() == true) {
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

fun parseFullReferent(s: String, precision: Int, ignoreDefault: Boolean): String? {
    val singleRef = parsePersonalReference(s)
    if (singleRef != null) {
        return singleRef.toString(precision, ignoreDefault)
    }
    val singleJoined = s.toCharArray().map { parsePersonalReference(it.toString()) }
    if (singleJoined.none { it == null }) {
        return singleJoined.requireNoNulls()
                .sortedBy { (it[0] as Enum<*>).ordinal }
                .joinToString(REFERENT_SEPARATOR, REFERENT_START, REFERENT_END) { it.toString(precision, ignoreDefault) }
    } else if (s.length == 3) {
        if (s.endsWith("ç") || s.endsWith("h") || s.endsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s[0].toString()) to parsePersonalReference(s.substring(1, 3))
            return if (ref1 != null && ref2 != null) {
                REFERENT_START +
                        ref1.toString(precision, ignoreDefault) +
                        REFERENT_SEPARATOR +
                        ref2.toString(precision, ignoreDefault) + REFERENT_END
            } else {
                null
            }
        } else if (s[1] == 'ç' || s[1] == 'h' || s.startsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s.substring(0, 2)) to parsePersonalReference(s[2].toString())
            return if (ref1 != null && ref2 != null) {
                REFERENT_START +
                        ref1.toString(precision, ignoreDefault) +
                        REFERENT_SEPARATOR +
                        ref2.toString(precision, ignoreDefault) +
                        REFERENT_END
            } else {
                null
            }
        }
    } else if (s.length == 4) {
        val halves = s.chunked(2)
                .map { r -> parsePersonalReference(r) }
        return if (halves.none { it == null }) {
            halves.requireNoNulls()
                    .sortedBy { (it[0] as Enum<*>).ordinal }
                    .joinToString(REFERENT_SEPARATOR, REFERENT_START, REFERENT_END) { it.toString(precision, ignoreDefault) }
        } else {
            null
        }
    }
    return null
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
               isShortcut: Boolean = false) : String {
    if (vx == CA_STACKING_VOWEL) {
        val ca = parseCa(cs)?.toString(precision, ignoreDefault) ?: return "(Unknown Ca)"
        return if (ca.isNotEmpty()) {
            "($ca)"
        } else {
            "(${Similarity.UNIPLEX.toString(precision, ignoreDefault = false)})"
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

        val case = Case.byVowel(vc)?.toString(precision) ?: return "(Unknown case)"
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

    val t = if (!isShortcut) when (type) {
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
    val root = rootData.find { it.cr == c } ?: return MarkdownUtil.bold(c.defaultForm()) to false
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
