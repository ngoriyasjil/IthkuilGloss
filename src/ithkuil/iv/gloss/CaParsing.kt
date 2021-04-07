package ithkuil.iv.gloss

data class CaForms(
    val configuration: String,
    val extension: String,
    val affiliation: String,
    val perspectiveAndEssence: String,
)

fun gatherCaValues(ca: String): CaForms? {
    val configuration = "[lrř]?[tkp]|r?[nňm]".toRegex()
    val extension = "[sšfţç]".toRegex()
    val affiliation = "^[dgb]|[tkp]".toRegex()
    val perspectiveEssence = "^tļ|^l[mn]|^[lrvzř]|[rvwyřlmhnç]".toRegex()

    val fullRegex = (
        "($configuration)?" +
            "($extension)?" +
            "($affiliation)?" +
            "($perspectiveEssence)?"
        ).toRegex()

    val matches = fullRegex
        .matchEntire(ca)
        ?.groups
        ?.drop(1)
        ?.map { it?.value ?: "" } ?: return null

    return CaForms(matches[0], matches[1], matches[2], matches[3])
}

fun parseCa(ca: String): Slot? {
    val forms = gatherCaValues(ca.substituteAll(CA_SUBSTITUTIONS)) ?: return null

    val configuration = when (forms.configuration) {
        "" -> Configuration.UNIPLEX
        "rt" -> Configuration.DUPLEX_SIMILAR_SEPARATE
        "rk" -> Configuration.DUPLEX_SIMILAR_CONNECTED
        "rp" -> Configuration.DUPLEX_SIMILAR_FUSED
        "rn" -> Configuration.DUPLEX_DISSIMILAR_SEPARATE
        "rň" -> Configuration.DUPLEX_DISSIMILAR_CONNECTED
        "rm" -> Configuration.DUPLEX_DISSIMILAR_FUSED
        "řt" -> Configuration.DUPLEX_FUZZY_SEPARATE
        "řk" -> Configuration.DUPLEX_FUZZY_CONNECTED
        "řp" -> Configuration.DUPLEX_FUZZY_FUSED
        "t" -> Configuration.MULTIPLEX_SIMILAR_SEPARATE
        "k" -> Configuration.MULTIPLEX_SIMILAR_CONNECTED
        "p" -> Configuration.MULTIPLEX_SIMILAR_FUSED
        "n" -> Configuration.MULTIPLEX_DISSIMILAR_SEPARATE
        "ň" -> Configuration.MULTIPLEX_DISSIMILAR_CONNECTED
        "m" -> Configuration.MULTIPLEX_DISSIMILAR_FUSED
        "lt" -> Configuration.MULTIPLEX_FUZZY_SEPARATE
        "lk" -> Configuration.MULTIPLEX_FUZZY_CONNECTED
        "lp" -> Configuration.MULTIPLEX_FUZZY_FUSED
        else -> return null
    }

    val extension = when (forms.extension) {
        "" -> Extension.DELIMITIVE
        "s" -> Extension.PROXIMAL
        "š" -> Extension.INCIPIENT
        "f" -> Extension.ATTENUATIVE
        "ţ" -> Extension.GRADUATIVE
        "ç" -> Extension.DEPLETIVE
        else -> return null
    }

    val affiliation = when (forms.affiliation) {
        "" -> Affiliation.CONSOLIDATIVE
        "d", "t" -> Affiliation.ASSOCIATIVE
        "g", "k" -> Affiliation.COALESCENT
        "b", "p" -> Affiliation.VARIATIVE
        else -> return null
    }

    val (c, e, a, _) = forms
    val (perspective, essence) =
        if (listOf(c, e, a).all { it.isEmpty() }) {
            when (forms.perspectiveAndEssence) {
                "l" -> Perspective.MONADIC to Essence.NORMAL
                "r" -> Perspective.POLYADIC to Essence.NORMAL
                "v" -> Perspective.NOMIC to Essence.NORMAL
                "z" -> Perspective.ABSTRACT to Essence.NORMAL
                "ř" -> Perspective.MONADIC to Essence.REPRESENTATIVE
                "tļ" -> Perspective.POLYADIC to Essence.REPRESENTATIVE
                "lm" -> Perspective.NOMIC to Essence.REPRESENTATIVE
                "ln" -> Perspective.ABSTRACT to Essence.REPRESENTATIVE
                else -> return null
            }
        } else {
            when (forms.perspectiveAndEssence) {
                "" -> Perspective.MONADIC to Essence.NORMAL
                "r", "v" -> Perspective.POLYADIC to Essence.NORMAL
                "w" -> Perspective.NOMIC to Essence.NORMAL
                "y" -> Perspective.ABSTRACT to Essence.NORMAL
                "ř" -> Perspective.MONADIC to Essence.REPRESENTATIVE
                "l" -> Perspective.POLYADIC to Essence.REPRESENTATIVE
                "m", "h" -> Perspective.NOMIC to Essence.REPRESENTATIVE
                "n", "ç" -> Perspective.ABSTRACT to Essence.REPRESENTATIVE
                else -> return null
            }
        }

    return Slot(configuration, extension, affiliation, perspective, essence)

}