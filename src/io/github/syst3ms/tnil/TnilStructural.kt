package io.github.syst3ms.tnil

interface Precision {
    fun toString(precision: Int, ignoreDefault: Boolean = false): String
}

class Slot(private vararg val values: Precision?) : Precision {

    var stemUsed = false

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
}

fun List<Precision>.glossSlots(precision: Int, ignoreDefault: Boolean = false) : String {
    return map{ it.toString(precision, ignoreDefault) }
        .filter(String::isNotEmpty)
        .joinToString(SLOT_SEPARATOR)
}