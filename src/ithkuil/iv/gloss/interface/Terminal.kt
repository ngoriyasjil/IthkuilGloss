package ithkuil.iv.gloss.`interface`

import java.io.FileNotFoundException

fun main() {

    try {
        loadResourcesLocal()
    } catch (e: FileNotFoundException) {
        println("Loading resources...")
        loadResourcesOnline()
    }

    do {
        print(">>> ")
        val msg = readLine() ?: ""
        val response = respond(msg)
        if (response != null) {
            println(response)
        }
    } while (msg.isNotEmpty())
  }