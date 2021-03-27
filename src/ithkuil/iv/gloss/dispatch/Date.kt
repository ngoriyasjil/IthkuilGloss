package ithkuil.iv.gloss.dispatch

import ithkuil.iv.gloss.VOWEL_FORMS
import java.time.LocalDateTime
import java.time.ZoneId

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
    "šš",
)

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

fun  main() {
    println(datetimeInIthkuil(LocalDateTime.now()))
}

private fun digitPart(n: Int) : String {
    val units = n % 10
    val tens = n / 10
    val u = NUMBER_ROOTS[units]
    val t = if (tens > 0) "${VOWEL_FORMS[tens-1]}rs" else ""

    return "${u}${t}"
}

fun datetimeInIthkuil(date: LocalDateTime? = null) : String {

    val datetime = date ?: LocalDateTime.now(ZoneId.of("UTC"))

    val day = datetime.dayOfMonth

    val month =  datetime.monthValue

    val ardhal = "Wu${digitPart(day)}irwia${NUMBER_AFFIXES[month]}ó"

    val year = datetime.year

    val ernal = "wa${digitPart(year / 100)}a'o"
    val arnal = "wa${digitPart(year % 100)}ürwi'i"

    val urwal = "wu${digitPart(datetime.hour)}erwa'o"
    val erwal = "wa${digitPart(datetime.minute)}oň"


    return "$ardhal $ernal $arnal, $urwal $erwal"

}