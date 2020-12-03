package io.github.syst3ms.tnil

interface Precision {
    fun toString(precision: Int, ignoreDefault: Boolean = false): String
}

interface Category : Precision {
    val ordinal: Int
    val name: String
    val short: String

    override fun toString(precision: Int, ignoreDefault: Boolean) = when {
        ignoreDefault && this.ordinal == 0 -> ""
        precision >= 2 -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

interface NoDefault : Category {
    override fun toString(precision: Int, ignoreDefault: Boolean): String =
        super.toString(precision, false)
}

class Slot(private vararg val values: Precision?) : Precision {

    var stemUsed = false

    val size: Int
        get() = values.size

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return values
            .filterNotNull()
            .map {
                val gloss = it.toString(precision, ignoreDefault)
                if (stemUsed && it is Stem) "__${gloss}__" else gloss
            }
            .filter(String::isNotEmpty)
            .joinToString(CATEGORY_SEPARATOR)
    }

    fun getStem() : Int? {
        return (values.find { it is Stem } as? Stem)?.ordinal
    }
}

fun List<Precision>.glossSlots(precision: Int, ignoreDefault: Boolean = false) : String {
    return map{ it.toString(precision, ignoreDefault) }
        .filter(String::isNotEmpty)
        .joinToString(SLOT_SEPARATOR)
}