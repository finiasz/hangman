package net.finiasz.pendu

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import net.finiasz.pendu.database.AppDatabase
import net.finiasz.pendu.database.Word
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern
import kotlin.random.Random


class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())

    val state : StateFlow<GameState> = _state.asStateFlow()

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun reset(settingsManager: SettingsManager, dispatcher: CoroutineDispatcher = Dispatchers.IO) = withContext(dispatcher) {
        val count = settingsManager.wordsCount.value
        val word : Word?
        if (count != null) {
            val num = Random.nextInt(count)
            word = when (settingsManager.dictionary.value) {
                "fr_xs" -> AppDatabase.getInstance().wordDao().pickInFrXs(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                "fr_m" -> AppDatabase.getInstance().wordDao().pickInFrM(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                "fr_l" -> AppDatabase.getInstance().wordDao().pickInFrL(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                "fr_xxl" -> AppDatabase.getInstance().wordDao().pickInFrXxl(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                "en_xs" -> AppDatabase.getInstance().wordDao().pickInEnXs(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                "en_s" -> AppDatabase.getInstance().wordDao().pickInEnS(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                "en_l" -> AppDatabase.getInstance().wordDao().pickInEnL(
                    num,
                    settingsManager.minLength.value,
                    settingsManager.maxLength.value
                )

                else -> null
            }
        } else {
            word = null
        }
        val mot = word?.mot?.uppercase(if (settingsManager.dictionary.value.startsWith("fr")) Locale.FRANCE else Locale.UK )
        val motUnaccented = mot?.unAccent()
        val lettresTrouvees: MutableList<Boolean?> = MutableList(mot?.length ?: 0) { false }
        motUnaccented?.forEachIndexed { index, char ->
            if (char < 'A' || char > 'Z') {
                lettresTrouvees[index] = null
            }
        }

        var lettresManquantes = lettresTrouvees.fold(0) { acc, b -> if (b == false) acc + 1 else acc }

        when (settingsManager.freeLetters.value) {
            3 -> {
                if (lettresManquantes >= 2) {
                    lettresTrouvees[lettresTrouvees.indexOf(false)] = null
                    lettresTrouvees[lettresTrouvees.lastIndexOf(false)] = null
                    lettresManquantes -= 2
                }
            }

            2 -> {
                if (lettresManquantes >= 1) {
                    lettresTrouvees[lettresTrouvees.lastIndexOf(false)] = null
                    lettresManquantes--
                }
            }

            1 -> {
                if (lettresManquantes >= 1) {
                    lettresTrouvees[lettresTrouvees.indexOf(false)] = null
                    lettresManquantes--
                }
            }
        }

        val value = GameState(mot = mot, motUnaccented = motUnaccented, lettresManquantes = lettresManquantes, erreursRestantes = settingsManager.totalAttempts.value, erreursMax = settingsManager.totalAttempts.value, lettresTrouvees = lettresTrouvees)

        _state.update { value }
        for (i in 0..<settingsManager.discardLetters.value) {
            discardClick()
        }
    }


    fun lettreClick(lettre : Char) {
        val lettrePos = lettre - 'A'
        if (state.value.mot == null || state.value.lettresTrouvees == null) {
            return
        }
        if (state.value.won != Won.NOT_WON) {
            return
        }
        if (state.value.letterStates[lettrePos] != LetterState.UNKNOWN) {
            return
        }

        _state.update {
            var lettresManquantes = it.lettresManquantes
            var erreursRestantes = it.erreursRestantes
            val lettresTrouvees = it.lettresTrouvees!!.toMutableList()
            val letterStates = it.letterStates.toMutableList()
            var won = it.won

            var erreur = true

            // check if the letter appears in the mot
            it.motUnaccented!!.forEachIndexed {  index, let ->
                if (lettresTrouvees[index]?.not() == true) {
                    if (let == lettre) {
                        erreur = false
                        lettresManquantes--
                        lettresTrouvees[index] = true
                    }
                }
            }

            if (erreur) {
                letterStates[lettrePos] = LetterState.WRONG
                erreursRestantes--
            } else {
                letterStates[lettrePos] = LetterState.CORRECT
            }

            if (lettresManquantes == 0) {
                won = Won.WON
            } else if (erreursRestantes == 0) {
                won = Won.LOST
            }


            it.copy(lettresManquantes = lettresManquantes, erreursRestantes =  erreursRestantes, lettresTrouvees =  lettresTrouvees, letterStates = letterStates, won = won)
        }
    }

    fun revealClick() {
        if (state.value.mot == null || state.value.lettresTrouvees == null) {
            return
        }
        if (state.value.won != Won.NOT_WON) {
            return
        }
        if (state.value.lettresManquantes == 0) {
            return
        }

        val picked = state.value.pickLetter() ?: return


        _state.update {
            var lettresManquantes = it.lettresManquantes
            val lettresTrouvees = it.lettresTrouvees!!.toMutableList()
            val letterStates = it.letterStates.toMutableList()
            var won = it.won

            val lettrePos = picked - 'A'

            letterStates[lettrePos] = LetterState.HINT_PRESENT

            it.motUnaccented!!.forEachIndexed {  index, let ->
                if (lettresTrouvees[index]?.not() == true) {
                    if (let == picked) {
                        lettresManquantes--
                        lettresTrouvees[index] = true
                    }
                }
            }

            if (lettresManquantes == 0) {
                won = Won.WON
            }

            it.copy(lettresManquantes = lettresManquantes, lettresTrouvees =  lettresTrouvees, letterStates = letterStates, won = won)

        }
    }


    fun discardClick() {
        if (state.value.mot == null || state.value.lettresTrouvees == null) {
            return
        }
        if (state.value.won != Won.NOT_WON) {
            return
        }
        if (!state.value.unusedLetterExists()) {
            return
        }

        val picked = state.value.pickUnusedLetter() ?: return

        _state.update {
            val letterStates = it.letterStates.toMutableList()
            letterStates[picked - 'A'] = LetterState.HINT_ABSENT

            it.copy(letterStates = letterStates)
        }
    }
}

private val unAccentPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
fun String.unAccent(): String {
    return unAccentPattern
        .matcher(Normalizer.normalize(this, Normalizer.Form.NFD))
        .replaceAll("")
        .uppercase(Locale.FRANCE)
}
