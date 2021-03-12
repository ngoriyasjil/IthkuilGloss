package ithkuil.iv.gloss

import mu.KotlinLogging

val logger = KotlinLogging.logger { }

var affixData: Map<String, AffixData> = emptyMap()
var rootData:  Map<String, RootData>  = emptyMap()

data class AffixData(val abbreviation: String, val descriptions: List<String>)

fun parseAffixes(data: String): Map<String, AffixData> = data
        .lineSequence()
        .drop(1)
        .map       { it.split("\t") }
        .filter    { it.size >= 11 }
        .associate { it[0] to AffixData(it[1], it.subList(2, 11)) }

data class RootData(val descriptions: List<String>)

fun parseRoots(data: String): Map<String, RootData> = data
        .lineSequence()
        .drop(1)
        .map       { it.split("\t") }
        .filter    { it.size >= 5 }
        .associate { it[0] to RootData(it.subList(1, 5)) }
