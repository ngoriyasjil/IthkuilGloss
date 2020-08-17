package io.github.syst3ms.tnil

import net.dv8tion.jda.api.utils.MarkdownUtil
import java.io.File
import kotlin.streams.toList

const val AFFIX_PATH = "./affixes.txt"
const val ROOTS_PATH = "./roots.txt"

val affixData: List<AffixData> by lazy {
    loadAffixes()
}
val rootData: List<RootData> by lazy {
    loadRoots()
}

fun String.isVowel() = this.defaultForm() in flatVowelForm

fun String.isConsonant() = this.all { it.toString().defaultForm() in CONSONANTS }

fun String.isModular() = this matches "'?([wy]|h(?:lw)?.*)".toRegex()

fun String.hasStress() = this.isVowel() && this.defaultForm() != this

fun String.isInvalidLexical() = this.defaultForm() in INVALID_LEXICAL_CONSONANTS || this.startsWith("h") || this.contains("'")

fun String.trimGlottal() = this.replace("'", "")

fun String.trimH() = this.replace("^('?)h".toRegex(), "$1")

fun String.plusSeparator(start: Boolean = false, sep: String = SLOT_SEPARATOR) = when {
    this.isEmpty() -> this
    start -> "$sep$this"
    else -> "$this$sep"
}

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

fun parseFullReferent(s: String, precision: Int, ignoreDefault: Boolean, final: Boolean = false): String? {
    val singleRef = parsePersonalReference(s)
    if (singleRef != null) {
        return singleRef.toString(precision, ignoreDefault)
    }
    val singleJoined = s.toCharArray().map { parsePersonalReference(it.toString(), final) }
    if (singleJoined.none { it == null }) {
        return singleJoined.requireNoNulls()
                .sortedBy { (it[0] as Enum<*>).ordinal }
                .joinToString(REFERENT_SEPARATOR, REFERENT_START, REFERENT_END) { it.toString(precision, ignoreDefault) }
    } else if (s.length == 3) {
        if (s.endsWith("ç") || s.endsWith("h") || s.endsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s[0].toString(), final) to parsePersonalReference(s.substring(1, 3), final)
            return if (ref1 != null && ref2 != null) {
                REFERENT_START +
                        ref1.toString(precision, ignoreDefault) +
                        REFERENT_SEPARATOR +
                        ref2.toString(precision, ignoreDefault) + REFERENT_END
            } else {
                null
            }
        } else if (s[1] == 'ç' || s[1] == 'h' || s.startsWith("rr")) {
            val (ref1, ref2) = parsePersonalReference(s.substring(0, 2), final) to parsePersonalReference(s[2].toString(), final)
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
                .map { r -> parsePersonalReference(r, final) }
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

fun File.bufferedReaderOrNull() = if (this.exists()) bufferedReader() else null

fun loadAffixes() = File(AFFIX_PATH).bufferedReaderOrNull()
        ?.lines()
        ?.filter { it.isNotBlank() }
        ?.map { it.split("|") }
        ?.map { AffixData(it[0], it[1], it.drop(2).toTypedArray()) }
        ?.toList() ?: emptyList()

fun parseAffix(c: String, v: String, precision: Int, ignoreDefault: Boolean, slotThree: Boolean = false): String {
    val vi = AFFIX_VOWELS.indexOfFirst { it eq v }
    if (vi == -1 && !(v eq CA_STACKING_VOWEL || c in CASE_AFFIXES)) {
        return "$AFFIX_UNKNOWN_VOWEL_MARKER$v"
    }
    val deg = vi % 10
    var specialFormMessage = ""
    val type = if (vi < 30) {
        vi / 10 + 1
    } else {
        specialFormMessage = when {
            slotThree && precision == 0 -> "{Stm}"
            slotThree -> "{stem only}"
            !slotThree && precision == 0 -> "{Incp+Main}"
            else -> "{both incorporated and main}"
        }
        vi / 10 - 2
    }
    val aff = affixData.find { it.cs == c }
    if (c == "rl") { // case-stacking affix
        val case = Case.byVowel(v)?.toString(precision) ?: return "$AFFIX_UNKNOWN_CASE_MARKER$v"
        return "($case$specialFormMessage)"
    } else if (c == "ll" || c == "rr") { // case-accessor affix
        val case = Case.byVowel(v)?.toString(precision) ?: return "$AFFIX_UNKNOWN_CASE_MARKER$v"
        return if (precision > 0) {
            "($case\\accessor-" + (if (c == "ll") "Type 1" else "Type 2") + specialFormMessage.plusSeparator(start = true) + ")"
        } else {
            "(${case}a-" + (if (c == "ll") "T1" else "T2") + specialFormMessage.plusSeparator(start = true) + ")"
        }
    } else if (c == "lw" || c == "ly") { // inverse case-accessor affix
        val case = Case.byVowel(v)?.toString(precision) ?: return "$AFFIX_UNKNOWN_CASE_MARKER$v"
        return if (precision > 0) {
            "($case\\inverse accessor-" + (if (c == "lw") "Type 1" else "Type 2") + specialFormMessage.plusSeparator(start = true) + ")"
        } else {
            "(${case}ia-" + (if (c == "lw") "T1" else "T2") + specialFormMessage.plusSeparator(start = true) + ")"
        }
    } else if (v eq CA_STACKING_VOWEL) {
        val ca = parseCa(c) ?: return "$AFFIX_UNKNOWN_CASE_MARKER$c"
        return if (slotThree) {
            perspectiveIndexFromCa(ca).toString() + AFFIX_STACKED_CA_MARKER
        } else {
            ""
        } + "(" + ca.toString(precision, ignoreDefault) + ")"
    }
    // Special cases
    return if (aff != null) {
        if (precision > 0 || deg == 0) {
            aff.desc.getOrNull(deg - 1)
                    ?.let { "'$it'" + (0x2080 + type).toChar() + specialFormMessage.plusSeparator(start = true, sep = CATEGORY_SEPARATOR) }
                    ?: "'" + aff.desc[0] + "'" +
                        (0x2080 + type).toChar() + specialFormMessage.plusSeparator(start = true, sep = CATEGORY_SEPARATOR) +
                        CATEGORY_SEPARATOR + deg
        } else {
            aff.abbr + (0x2080 + type).toChar() +
                    specialFormMessage.plusSeparator(start = true, sep = CATEGORY_SEPARATOR) + CATEGORY_SEPARATOR + deg
        }
    } else {
        MarkdownUtil.bold(c.defaultForm()) + (0x2080 + type).toChar() +
                specialFormMessage.plusSeparator(start = true, sep = CATEGORY_SEPARATOR) + CATEGORY_SEPARATOR + deg
    }
}

fun tppAffixString(degree: Int, rtiAffixScope: String?, precision: Int): String {
    val aff = affixData.find { it.cs == TPP_AFFIX_CONSONANT }
    val scope = rtiAffixScope ?: "{Form}"
    return when (aff) {
        null -> "TPP₁$CATEGORY_SEPARATOR$degree"
        else -> parseAffix(TPP_AFFIX_CONSONANT, AFFIX_VOWELS[degree - 1], precision, ignoreDefault = false)
    }.plusSeparator(sep = CATEGORY_SEPARATOR) + scope
}

fun loadRoots() = File(ROOTS_PATH).bufferedReaderOrNull()
        ?.lines()
        ?.map { it.split("|") }
        ?.map { RootData(it[0], it.subList(1, it.size)) }
        ?.toList() ?: emptyList()

fun parseRoot(c: String, precision: Int, stem: Int = 0): Pair<String, Boolean> {
    val root = rootData.find { it.cr == c } ?: return MarkdownUtil.bold(c.defaultForm()) to false
    if (precision > 0) {
        var stemUsed = false
        val d = (when (root.dsc.size) {
            1 -> root.dsc[0] // Only basic description, no precise stem description
            4, 7 -> {
                stemUsed = true
                root.dsc[stem] // basic description + IFL Stems 1,2,3
            }
            9 -> { // basic description + IFL Stems 0,1,2,3 ; only used for the carrier root
                stemUsed = true
                root.dsc[stem + 1]
            }
            else -> {
                // basic description + IFL & FML Stems 0,1,2,3 ; only used for the carrier root
                throw IllegalArgumentException("Root format is invalid : found ${root.dsc.size} arguments in the description of root -${root.cr}-")
            }
        }).toLowerCase()
        return "'$d'" to stemUsed
    } else {
        return "'${root.dsc[0].toLowerCase()}'" to false
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

data class PersonalReferentParsingData(var isInanimate: Boolean = false, var stem: Int = 1)

class SentenceParsingState(var carrier: Boolean = false,
                                var register: Register? = null,
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