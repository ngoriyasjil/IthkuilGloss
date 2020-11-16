package io.github.syst3ms.tnil


fun main() {
    do {
        print(">>> ")
        val word = readLine() ?: ""
        try {
            println(parseWord(word,1, true ))
        } catch (ex: Exception){
            println(ex.message ?: "No error message")
        }
    } while (word.isNotEmpty())
    }