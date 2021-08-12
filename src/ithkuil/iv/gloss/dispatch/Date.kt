package ithkuil.iv.gloss.dispatch

import ithkuil.iv.gloss.Stem
import ithkuil.iv.gloss.VOWEL_FORMS
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val NUMBER_ROOTS = listOf(
    "vr",
    "ll",
    "ks",
    "z",
    "pš",
    "st",
    "cp",
    "ns",
    "čk",
    "lẓ",
    "j",
)

private val INVALID_INITIAL_NUMBER_ROOTS = setOf("ns", "lẓ", "čk")

private val NUMBER_AFFIXES = listOf(
    "vc",
    "zc",
    "ks",
    "z",
    "pš",
    "st",
    "cp",
    "ns",
    "čk",
    "lẓ",
    "j",
    "cg",
    "jd"
)

private fun numeralBody(n: Int, shortcut: Boolean = false, stem: Stem = Stem.ONE): String {
    val units = n % 10
    val tens = n / 10

    val unitsRoot = NUMBER_ROOTS[units]
    val tensCs = if (tens > 0) "${VOWEL_FORMS[tens - 1]}rs" else ""

    val cc = if (shortcut) "w" else ""

    val vvElides = !shortcut && stem == Stem.ONE && unitsRoot !in INVALID_INITIAL_NUMBER_ROOTS
    val vv = if (vvElides) "" else when (stem) {
        Stem.ONE -> "a"
        Stem.TWO -> "e"
        Stem.THREE -> "u"
        Stem.ZERO -> "o"
    }

    val vrCa = if (!shortcut) "al" else ""


    return "$cc$vv$unitsRoot$vrCa$tensCs"
}

fun datetimeInIthkuil(
    datetime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
): String {

    val day = datetime.dayOfMonth
    val month = datetime.monthNumber
    val year = datetime.year

    val ardhal = "${numeralBody(day, shortcut = true, Stem.THREE)}ëirwia${NUMBER_AFFIXES[month]}iktó"

    val ernal = "${numeralBody(year / 100)}a'o"
    val arnal = "${numeralBody(year % 100)}ürwi'i"

    val urwal = "${numeralBody(datetime.hour, shortcut = true, Stem.THREE)}erwa'o"
    val erwal = "${numeralBody(datetime.minute)}oň"


    return "$ardhal $ernal $arnal ($urwal $erwal)".replaceFirstChar { it.uppercase() }

}