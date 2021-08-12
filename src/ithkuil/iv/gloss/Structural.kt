package ithkuil.iv.gloss

interface Resources {
    fun getAffix(cs: String): AffixData?
    fun getRoot(cr: String): RootData?
}

data class AffixData(val abbreviation: String, val descriptions: List<String>) {
    operator fun get(degree: Degree) = descriptions[degree.ordinal]
}

data class RootData(val descriptions: List<String>) {
    operator fun get(stem: Stem) = descriptions[stem.ordinal]
}


interface Glossable {
    fun gloss(o: GlossOptions): String
    fun checkDictionary(r: Resources): Glossable = this
}

interface Category : Glossable {
    val ordinal: Int
    val name: String
    val short: String

    override fun gloss(o: GlossOptions) = when {
        !o.includeDefaults && ordinal == 0 -> ""
        o.verbose -> name.lowercase()
        else -> short
    }
}

interface NoDefault : Category {
    override fun gloss(o: GlossOptions): String =
        super.gloss(o.showDefaults())
}

sealed class ContextOutcome
class Foreign(val word: String) : ContextOutcome()

sealed class ParseOutcome : ContextOutcome()
class Error(val message: String) : ParseOutcome()

open class Parsed(
    private val slots: List<Glossable>,
    private val stressMarked: Glossable? = null,
) : ParseOutcome(), Glossable {

    constructor(vararg slots: Glossable?, stressMarked: Glossable? = null) :
        this(slots.filterNotNull(), stressMarked = stressMarked)

    override fun gloss(o: GlossOptions): String {
        val mainWord = slots
            .map { it.gloss(o) }
            .filter(String::isNotEmpty)
            .joinToString(SLOT_SEPARATOR)
        val stressCategory = stressMarked?.gloss(o)
            .let { if (!it.isNullOrEmpty()) "$STRESS_SLOT_SEPARATOR$it" else "" }

        return mainWord + stressCategory
    }

    override fun checkDictionary(r: Resources): Parsed {
        val newSlots = slots.map { it.checkDictionary(r) }

        return Parsed(newSlots, stressMarked = stressMarked)
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun addPrefix(prefix: Glossable): Parsed {
        val newSlots = buildList {
            add(prefix)
            addAll(slots)
        }

        return Parsed(newSlots, stressMarked = stressMarked)
    }
}

enum class Precision {
    REGULAR,
    SHORT,
    FULL,
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


class Slot(private val values: List<Glossable>) : Glossable, List<Glossable> by values {

    constructor(vararg values: Glossable?) : this(values.filterNotNull())

    override fun gloss(o: GlossOptions): String {
        return values
            .map { it.gloss(o) }
            .filter(String::isNotEmpty)
            .joinToString(CATEGORY_SEPARATOR)
    }

    override fun toString(): String {
        val slotValues = values
            .joinToString { it.gloss(GlossOptions(Precision.FULL, includeDefaults = true)) }
        return "Slot($slotValues)"
    }

    override fun checkDictionary(r: Resources): Slot = Slot(values.map { it.checkDictionary(r) })
}

class ConcatenationChain(private val formatives: List<Parsed>) : Parsed() {

    override fun gloss(o: GlossOptions): String {
        return formatives
            .map { it.gloss(o) }
            .filter(String::isNotEmpty)
            .joinToString(CONCATENATION_SEPARATOR)
    }

    override fun checkDictionary(r: Resources): ConcatenationChain {
        return ConcatenationChain(formatives.map { it.checkDictionary(r) })
    }
}

class GlossString(
    private val full: String,
    private val normal: String = full,
    private val short: String = normal,
    private val ignorable: Boolean = false
) : Glossable {

    override fun gloss(o: GlossOptions): String {
        return when {
            ignorable && !o.includeDefaults -> ""
            o.concise -> short
            o.verbose -> full
            else -> normal
        }
    }
}

class Shown(private val value: Glossable, private val condition: Boolean = true) : Glossable {

    override fun gloss(o: GlossOptions): String = value.gloss(o.showDefaults(condition))

}

class Underlineable<T : Glossable>(val value: T, var used: Boolean = false) : Glossable {

    override fun gloss(o: GlossOptions): String = value.gloss(o).let { if (used) "__${it}__" else it }

}

class ForcedDefault(
    private val value: Glossable,
    private val default: String,
    private val condition: Boolean = true
) : Glossable {

    override fun gloss(o: GlossOptions): String =
        value.gloss(o).let { if (it.isEmpty() && condition) default else it }

}

class Root(private val cr: String, private val stem: Underlineable<Stem>) : Glossable {

    private var description: String = "**$cr**"

    override fun checkDictionary(r: Resources): Root {

        val rootEntry = r.getRoot(cr)

        if (rootEntry != null) {

            val stemDesc = rootEntry[stem.value]

            description = if (stemDesc.isNotEmpty()) {
                stem.used = true
                "“$stemDesc“"
            } else {
                "“${rootEntry[Stem.ZERO]}“"
            }
        }
        return this
    }

    override fun gloss(o: GlossOptions): String = description
}