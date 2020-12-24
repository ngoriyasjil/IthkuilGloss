package io.github.syst3ms.tnil

sealed class GlossOutcome
class Error(val message: String) : GlossOutcome()
open class Gloss(private vararg val slots: Glossable?, private val ignorable: Boolean = true) : GlossOutcome(), Glossable {

    override fun toString(o: GlossOpts): String {
        return slots
            .filterNotNull()
            .map { it.toString(o.withDefaults(!ignorable)) }
            .filter(String::isNotEmpty)
            .joinToString(SLOT_SEPARATOR)
    }

    fun addPrefix(prefix: Glossable?): Gloss = Gloss(prefix, *slots)
}

interface Glossable {
    fun toString(o: GlossOpts): String
}

class GlossOpts(val precision: Precision, val includeDefaults: Boolean = false) {
    fun withDefaults(onConditionThat: Boolean = true): GlossOpts =
        GlossOpts(this.precision, this.includeDefaults || onConditionThat)
    override fun toString(): String = "$precision form with ${if(includeDefaults) "" else "no "}defaults"

    val concise: Boolean
        get() = this.precision.concise
    val verbose: Boolean
        get() = this.precision.verbose
    val debug:   Boolean
        get() = this.precision.debug
}

enum class Precision(val concise: Boolean, val verbose: Boolean, val debug: Boolean) {
    REGULAR(false, false, false),
    SHORT  (true,  false, false),
    FULL   (false, true,  false),
    DEBUG  (false, true,  true);
}

interface Category : Glossable {
    val ordinal: Int
    val name: String
    val short: String

    override fun toString(o: GlossOpts) = when {
        !o.includeDefaults && this.ordinal == 0 -> ""
        o.verbose -> this.name.toLowerCase().replace("_", " ")
        else -> short
    }
}

interface NoDefault : Category {
    override fun toString(o: GlossOpts): String =
        super.toString(o.withDefaults())
}

class Slot(private vararg val values: Glossable?) : Glossable {

    var stemAvailable = false
    var default = ""

    val size: Int
        get() = values.size

    override fun toString(o: GlossOpts): String {
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

    override fun toString(o: GlossOpts): String {
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

    override fun toString(o: GlossOpts): String {
        return when {
            ignorable && !o.includeDefaults -> ""
            o.concise -> short
            o.verbose -> full
            else      -> normal
        }
    }
}
