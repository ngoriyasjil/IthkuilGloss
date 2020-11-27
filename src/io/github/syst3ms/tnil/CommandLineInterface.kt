package io.github.syst3ms.tnil


fun main() {

    println("Loading lexicon...")

    respond("?!reload")

    do {
        print(">>> ")
        val msg = readLine() ?: ""
        val response = respond(msg)
        if (response != null) {
            println(response)
        }
    } while (msg.isNotEmpty())
  }
