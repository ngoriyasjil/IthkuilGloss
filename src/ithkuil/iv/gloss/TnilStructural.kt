package ithkuil.iv.gloss

sealed class GlossOutcome

class Error(val message: String) : GlossOutcome()

open class Gloss(
    private vararg val slots: Glossable?,
    private val stressMarked: Glossable? = null,
    private val ignorable: Boolean = true,
) :
    GlossOutcome(), Glossable {

    override fun toString(o: GlossOptions): String {
        val mainWord = slots
            .filterNotNull()
            .map { it.toString(o.showDefaults(!ignorable)) }
            .filter(String::isNotEmpty)
            .joinToString(SLOT_SEPARATOR)
        val stressCategory = stressMarked?.toString(o.showDefaults(!ignorable))
            ?.let { "$STRESS_SLOT_SEPARATOR$it" } ?: ""

        return mainWord + stressCategory

    }

    fun addPrefix(prefix: Glossable?): Gloss = Gloss(prefix, *slots)
}

class GlossOptions(private val precision: Precision = Precision.REGULAR, val includeDefaults: Boolean = false) {

    fun showDefaults(condition: Boolean = true) =
        GlossOptions(this.precision, this.includeDefaults || condition)

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

interface Glossable {
    fun toString(o: GlossOptions): String
}



interface Category : Glossable {
    val ordinal: Int
    val name: String
    val short: String

    override fun toString(o: GlossOptions) = when {
        !o.includeDefaults && this.ordinal == 0 -> ""
        o.verbose -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

interface NoDefault : Category {
    override fun toString(o: GlossOptions): String =
        super.toString(o.showDefaults())
}

class Slot(private vararg val values: Glossable?) : Glossable {

    var stemAvailable = false
    var default = ""

    val size: Int
        get() = values.size

    override fun toString(o: GlossOptions): String {
        return values
            .filterNotNull()
            .map {
                val gloss = it.toString(o)
                if (stemAvailable && it is Stem && !o.concise) "__${gloss}__" else gloss
            }
            .filter(String::isNotEmpty)
            .joinToString(CATEGORY_SEPARATOR)
            .let { if (it.isNotEmpty()) it else default }
    }

    fun getStem() : Int? {
        return (values.find { it is Stem } as? Stem)?.ordinal
    }
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
