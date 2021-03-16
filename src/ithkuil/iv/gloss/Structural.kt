package ithkuil.iv.gloss

import ithkuil.iv.gloss.dispatch.AffixData
import ithkuil.iv.gloss.dispatch.RootData

sealed class GlossOutcome

class Error(val message: String) : GlossOutcome()

class Foreign(val word: String) : GlossOutcome()

open class Gloss(
    private val slots: List<Glossable>,
    private val stressMarked: Glossable? = null,
) : GlossOutcome(), Glossable {

    constructor(vararg slots: Glossable?, stressMarked: Glossable? = null) :
        this(slots.filterNotNull(), stressMarked = stressMarked)

    override fun toString(o: GlossOptions): String {
        val mainWord = slots
            .map { it.toString(o) }
            .filter(String::isNotEmpty)
            .joinToString(SLOT_SEPARATOR)
        val stressCategory = stressMarked?.toString(o)
            .let { if (!it.isNullOrEmpty()) "$STRESS_SLOT_SEPARATOR$it" else "" }

        return mainWord + stressCategory

    }

    override fun checkDictionary(r: Resources): Gloss {
        val newSlots = slots.map { it.checkDictionary(r) }

        return Gloss(newSlots, stressMarked = stressMarked)
    }


    fun addPrefix(prefix: Glossable): Gloss {
        val newSlots = mutableListOf(prefix).apply {
            addAll(slots)
        }

        return Gloss(newSlots, stressMarked = stressMarked)
    }
}

class GlossOptions(
    private val precision: Precision = Precision.REGULAR,
    val includeDefaults: Boolean = false,
) {

    fun showDefaults(condition: Boolean = true) =
        GlossOptions(precision, includeDefaults || condition)

    override fun toString(): String = "${precision.name} form ${if (includeDefaults) "with defaults" else ""}"

    val concise: Boolean
        get() = (precision == Precision.SHORT)
    val verbose: Boolean
        get() = (precision == Precision.FULL)
}

enum class Precision {
    REGULAR,
    SHORT,
    FULL,
}

interface Resources {
    fun getAffix(cs: String) : AffixData?
    fun getRoot(cr: String) : RootData?
}

interface Glossable {
    fun toString(o: GlossOptions): String

    fun checkDictionary(r: Resources) : Glossable = this // Usually just does nothing
}



interface Category : Glossable {
    val ordinal: Int
    val name: String
    val short: String

    override fun toString(o: GlossOptions) = when {
        !o.includeDefaults && this.ordinal == 0 -> ""
        o.verbose -> this.name.toLowerCase()
        else -> short
    }
}

interface NoDefault : Category {
    override fun toString(o: GlossOptions): String =
        super.toString(o.showDefaults())
}

class Slot(private val values: List<Glossable>) : Glossable, List<Glossable> by values {

    constructor(vararg values: Glossable?) : this(values.filterNotNull())

    override fun toString(o: GlossOptions): String {
        return values
            .map {
                it.toString(o)
            }
            .filter(String::isNotEmpty)
            .joinToString(CATEGORY_SEPARATOR)
    }

    override fun checkDictionary(r: Resources): Slot = Slot(values.map { it.checkDictionary(r) })
}

class ConcatenationChain(private vararg val formatives: Gloss) : Gloss() {

    override fun toString(o: GlossOptions): String {
        return formatives
            .map { it.toString(o) }
            .filter(String::isNotEmpty)
            .joinToString(CONCATENATION_SEPARATOR)
    }
}

class GlossString(
    private val full: String,
    private val normal: String = full,
    private val short: String = normal,
    private val ignorable: Boolean = false
) : Glossable {

    override fun toString(o: GlossOptions): String {
        return when {
            ignorable && !o.includeDefaults -> ""
            o.concise -> short
            o.verbose -> full
            else      -> normal
        }
    }
}

class Shown(private val value: Glossable, private val condition : Boolean = true) : Glossable {

    override fun toString(o: GlossOptions): String = value.toString(o.showDefaults(condition))

}

class Underline<T: Glossable>(val value: T, var used: Boolean = false) : Glossable {

    override fun toString(o: GlossOptions): String = value.toString(o).let { if (used) "__${it}__" else it }

}

class ForcedDefault(private val value: Glossable, val default: String) : Glossable {

    override fun toString(o: GlossOptions): String = value.toString(o).let { if (it.isEmpty()) default else it }

}