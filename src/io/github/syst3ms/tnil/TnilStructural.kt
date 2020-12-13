package io.github.syst3ms.tnil

sealed class GlossOutcome
class Error(val message: String) : GlossOutcome()
open class Gloss(private vararg val slots: Glossable?, private val ignorable: Boolean = true) : GlossOutcome(), Glossable {
    val size: Int
        get() = slots.size

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return slots
            .filterNotNull()
            .map {
                it.toString(precision, ignorable && ignoreDefault)
            }
            .filter(String::isNotEmpty)
            .joinToString(SLOT_SEPARATOR)
    }

    fun addPrefix(prefix: Glossable?): Gloss = Gloss(prefix, *slots)
}

interface Glossable {
    fun toString(precision: Int, ignoreDefault: Boolean = false): String
}

interface Category : Glossable {
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

class Slot(private vararg val values: Glossable?) : Glossable {

    var stemAvailable = false

    val size: Int
        get() = values.size

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return values
            .filterNotNull()
            .map {
                val gloss = it.toString(precision, ignoreDefault)
                if (stemAvailable && it is Stem && precision > 0) "__${gloss}__" else gloss
            }
            .filter(String::isNotEmpty)
            .joinToString(CATEGORY_SEPARATOR)
    }

    fun getStem() : Int? {
        return (values.find { it is Stem } as? Stem)?.ordinal
    }
}

class GlossString(
    private val full: String,
    private val normal: String = full,
    private val short: String = normal,
    private val ignorable: Boolean = false
) : Glossable {

    override fun toString(precision: Int, ignoreDefault: Boolean): String {
        return when {
            ignorable && ignoreDefault -> ""
            precision == 0 -> short
            precision < 2 -> normal
            else -> full
        }
    }
}