package io.github.syst3ms.tnil

import dev.kord.core.*

suspend fun main() {
    val token = tokenFile.readLines()[0]
    val kord = Kord(token)

    kord.login()
}