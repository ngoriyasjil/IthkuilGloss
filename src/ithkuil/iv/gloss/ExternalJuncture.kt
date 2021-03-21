package ithkuil.iv.gloss


fun externalJuncture(formatted : List<FormattingOutcome>) : String {

    val (valids, invalids) = formatted.partition { it is Valid }
        .let { (valids, invalids) ->
            Pair(
            valids.map { it as Valid },
            invalids.map { it as Invalid }
        )
    }

    if (invalids.isNotEmpty()) {
        return invalids.joinToString("\n") { "**$it**: *${it.message}*" }
    }


    val words: List<Word> = valids.flatMap {
        when(it) {
            is Word -> listOf(it)
            is ConcatenatedWords -> it.words
        }
    }

    val violations = mutableListOf<String>()

    for ((word1, word2) in words.pairs()) {
        if (word1.last().isConsonant() && word2.first().isConsonant()) {
            if (word1.postfixPunctuation.isEmpty() && word2.prefixPunctuation.isEmpty()) {
                violations.add("*EJ violation: $word1 $word2*")
            }
        }
    }

    if (violations.isEmpty()) return "*No EJ violations found*"

    return violations.joinToString("\n")
}

private fun <T> Iterable<T>.pairs() : Sequence<Pair<T, T>> = sequence {

    val iterator = iterator()

    var e1 = if (iterator.hasNext()) {
         iterator.next()
    } else return@sequence

    for (e2 in iterator) {
        yield(e1 to e2)
        e1 = e2
    }
}

