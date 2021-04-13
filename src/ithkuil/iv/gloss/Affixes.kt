package ithkuil.iv.gloss


enum class Degree(val numeral: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    ZERO(0);

    companion object {
        fun byForm(n: Int): Degree? = values().find { it.numeral == n }
    }
}

enum class AffixType(val subscript: String) {
    ONE("\u2081"),
    TWO("\u2082"),
    THREE("\u2083");

}

enum class CaseAffixKind(override val short: String) : NoDefault {
    CASE_ACCESSOR("acc"),
    INVERSE_ACCESSOR("ia"),
    CASE_STACKING("case:");
}

sealed class AffixOutcome

class AffixError(val message: String) : AffixOutcome()

sealed class ValidAffix : AffixOutcome(), Glossable

class CsAffix(private val cs: String, private val degree: Degree, private val type: AffixType? = null) : ValidAffix() {

    private var description: String? = null
    private var abbreviation: String? = "**$cs**"

    override fun checkDictionary(r: Resources): Glossable {

        val affixEntry = r.getAffix(cs) ?: return this

        abbreviation = affixEntry.abbreviation

        if (this.degree != Degree.ZERO) {
            description = affixEntry[degree]
        }

        return this

    }

    override fun toString(o: GlossOptions): String {

        return if (o.concise || degree == Degree.ZERO || description == null) {
            "$abbreviation$AFFIX_DEGREE_SEPARATOR${degree.numeral}${type?.subscript ?: ""}"
        } else {
            "‘$description‘${type?.subscript ?: ""}"
        }

    }

}

class CaStacker(private val ca: Slot) : ValidAffix() {
    override fun toString(o: GlossOptions): String = "(${ForcedDefault(ca, "default_ca").toString(o)})"
}

class CaseAffix(private val kind: CaseAffixKind, private val case: Case, private val type: AffixType) : ValidAffix() {
    override fun toString(o: GlossOptions): String = "(${kind.toString(o)}:${case.toString(o)})${type.subscript}"
}

class ReferentialShortcut(private val referents: Referential, private val case: Case) : ValidAffix() {
    override fun toString(o: GlossOptions): String = "(${referents.toString(o)}-${case.toString(o.showDefaults())})"
}


class Affix(private val vx: String, private val cs: String) {
    fun parse(canBeReferentialShortcut: Boolean = false): AffixOutcome {
        if (vx == CA_STACKING_VOWEL) {
            val ca = parseCa(cs) ?: return AffixError("Unknown stacked Ca: $cs")
            return CaStacker(ca)
        }

        if (cs in CASE_AFFIXES) {
            val vc = when (cs) {
                "sw", "zw", "čw", "šw", "žw", "jw", "lw" -> vx
                "sy", "zy", "čy", "šy", "žy", "jy", "ly" -> glottalizeVowel(vx)
                else -> return AffixError("Unknown case affix form: $cs")
            }

            val case = Case.byVowel(vc) ?: return AffixError("Unknown case vowel: $vx")

            val kind = when (cs) {
                "sw", "sy", "zw", "zy", "čw", "čy" -> CaseAffixKind.CASE_ACCESSOR
                "šw", "šy", "žw", "žy", "jw", "jy" -> CaseAffixKind.INVERSE_ACCESSOR
                "lw", "ly" -> CaseAffixKind.CASE_STACKING
                else -> return AffixError("Unknown case affix form: $cs")
            }

            val type = when (cs) {
                "sw", "sy", "šw", "šy" -> AffixType.ONE
                "zw", "zy", "žw", "žy" -> AffixType.TWO
                "čw", "čy", "jw", "jy" -> AffixType.THREE
                else -> return AffixError("Unknown case affix form: $cs")
            }

            return CaseAffix(kind, case, type)
        }

        val (series, form) = seriesAndForm(vx)

        if (canBeReferentialShortcut && series == 3 || series == 4) {
            val case = when (series) {
                3 -> when (form) {
                    1 -> Case.POSSESSIVE
                    2 -> Case.PROPRIETIVE
                    3 -> Case.GENITIVE
                    4 -> Case.ATTRIBUTIVE
                    5 -> Case.PRODUCTIVE
                    6 -> Case.INTERPRETIVE
                    7 -> Case.ORIGINATIVE
                    8 -> Case.INTERDEPENDENT
                    9 -> Case.PARTITIVE
                    else -> return AffixError("Unknown vowel form ($form): $vx")
                }
                4 -> when (form) {
                    1 -> Case.THEMATIC
                    2 -> Case.INSTRUMENTAL
                    3 -> Case.ABSOLUTIVE
                    4 -> Case.AFFECTIVE
                    5 -> Case.STIMULATIVE
                    6 -> Case.EFFECTUATIVE
                    7 -> Case.ERGATIVE
                    8 -> Case.DATIVE
                    9 -> Case.INDUCIVE
                    else -> return AffixError("Unknown vowel form ($form): $vx")
                }
                else -> return AffixError("Unknown referential shortcut series ($series): $vx")
            }

            val referential = parseFullReferent(listOf(cs)) ?: return AffixError("Unknown referential: $cs")

            return ReferentialShortcut(referential, case)
        }

        val degree = if (vx in setOf("ae", "ea", "äi")) {
            Degree.ZERO
        } else Degree.values().getOrNull(form - 1) ?: return AffixError("Unknown affix vowel form: $vx")

        val type: AffixType = if (degree == Degree.ZERO) when (vx) {
            "ae" -> AffixType.ONE
            "ea" -> AffixType.TWO
            "äi" -> AffixType.THREE
            else -> return AffixError("Unknown degree zero vowel: $vx")
        } else when (series) {
            1 -> AffixType.ONE
            2 -> AffixType.TWO
            3 -> AffixType.THREE
            else -> return AffixError("Unknown vowel series ($series): $vx")
        }

        return CsAffix(cs, degree, type)

    }
}

inline fun AffixOutcome.validate(dealWithError: (AffixError) -> Nothing): ValidAffix = when (this) {
    is ValidAffix -> this
    is AffixError -> dealWithError(this)
}

fun List<Affix>.parseAll(): List<AffixOutcome> = if (size == 1) {
    this[0]
        .parse(canBeReferentialShortcut = true)
        .let { listOf(it) }
} else {
    map(Affix::parse)
}

inline fun List<AffixOutcome>.validateAll(
    dealWithError: (AffixError) -> Nothing
): List<ValidAffix> = map { it.validate(dealWithError) }