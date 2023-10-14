package net.finiasz.pendu

import kotlin.random.Random

data class GameState(
    val mot : String? = null,
    val motUnaccented : String? = null,
    val lettresManquantes : Int = 0,
    val erreursMax : Int = 0,
    val erreursRestantes : Int = 0,
    val lettresTrouvees : MutableList<Boolean?>? = null, // an array the same length as the mot, indicating whether this letter was already found. Null means the letter was given at the start
    val letterStates : MutableList<LetterState> = MutableList(26) { LetterState.UNKNOWN },
    var won : Won = Won.NOT_WON,
) {
    fun unusedLetterExists() : Boolean {
        motUnaccented?.let {
            val unFoundCount = it.filterIndexed { index, c -> lettresTrouvees!![index] == false}.toCharArray().toSet().size
            val unOpenedCount = letterStates.count { it == LetterState.UNKNOWN }
            return  unFoundCount < unOpenedCount
        }
        return false
    }

    fun pickUnusedLetter() : Char? {
        motUnaccented?.let {
            val unFoundLetters = it.filterIndexed { index, c -> lettresTrouvees!![index] == false}.toCharArray().toSet()
            val unOpenedCount = letterStates.count { it == LetterState.UNKNOWN }
            var num = Random.nextInt(unOpenedCount - unFoundLetters.size)
            letterStates.forEachIndexed { index, state ->
                if (state == LetterState.UNKNOWN) {
                    if (!unFoundLetters.contains('A' + index)) {
                        if (num == 0) {
                            return 'A' + index
                        }
                        num--
                    }
                }
            }
        }
        return null
    }

    fun pickLetter() : Char? {
        motUnaccented?.let {
            val unFoundLetters = it.filterIndexed { index, c -> lettresTrouvees!![index] == false }.toCharArray().toSet().toList()
            val num = Random.nextInt(unFoundLetters.size)
            return unFoundLetters[num]
        }
        return null
    }
}


enum class Won {
    NOT_WON,
    WON,
    LOST,
}

enum class LetterState {
    UNKNOWN,
    WRONG,
    CORRECT,
    HINT_ABSENT,
    HINT_PRESENT,
}