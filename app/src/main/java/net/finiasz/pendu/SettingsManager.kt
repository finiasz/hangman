package net.finiasz.pendu

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import net.finiasz.pendu.database.AppDatabase

class SettingsManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val dictionary = mutableStateOf(getString(DICTIONARY, context.getString(R.string.default_dictionary)))
    val minLength = mutableIntStateOf(getInt(MIN_LENGTH, 5))
    val maxLength = mutableIntStateOf(getInt(MAX_LENGTH, 0))
    val freeLetters = mutableIntStateOf(getInt(FREE_LETTERS, 1))
    val discardLetters = mutableIntStateOf(getInt(DISCARD_LETTERS, 0))
    val totalAttempts = mutableIntStateOf(getInt(TOTAL_ATTEMPTS, 8))

    val voyelles = MutableStateFlow(getVoyelles())

    val wordsCount : MutableState<Int?> = mutableStateOf(null)

    suspend fun countWords(dispatcher: CoroutineDispatcher = Dispatchers.IO) = withContext(dispatcher) {
        val count = when(dictionary.value) {
            "fr_xs" -> AppDatabase.getInstance().wordDao().countInFrXs(minLength.intValue, maxLength.intValue)
            "fr_m" -> AppDatabase.getInstance().wordDao().countInFrM(minLength.intValue, maxLength.intValue)
            "fr_l" -> AppDatabase.getInstance().wordDao().countInFrL(minLength.intValue, maxLength.intValue)
            "fr_xxl" -> AppDatabase.getInstance().wordDao().countInFrXxl(minLength.intValue, maxLength.intValue)
            "en_xs" -> AppDatabase.getInstance().wordDao().countInEnXs(minLength.intValue, maxLength.intValue)
            "en_s" -> AppDatabase.getInstance().wordDao().countInEnS(minLength.intValue, maxLength.intValue)
            "en_l" -> AppDatabase.getInstance().wordDao().countInEnL(minLength.intValue, maxLength.intValue)
            else -> null
        }
        wordsCount.value = count
    }



    fun setDictionary(dictionary: String) {
        saveString(DICTIONARY, dictionary)
        this.dictionary.value = dictionary
        this.voyelles.value = getVoyelles()
    }

    fun setMinLength(minLength: Int) {
        saveInt(MIN_LENGTH, minLength)
        this.minLength.intValue = minLength
    }

    fun setMaxLength(maxLength: Int) {
        saveInt(MAX_LENGTH, maxLength)
        this.maxLength.intValue = maxLength
    }

    fun setFreeLetters(freeLetters: Int) {
        saveInt(FREE_LETTERS, freeLetters)
        this.freeLetters.intValue = freeLetters
    }

    fun setDiscardLetters(discardLetters: Int) {
        saveInt(DISCARD_LETTERS, discardLetters)
        this.discardLetters.intValue = discardLetters
    }


    fun setTotalAttempts(totalAttempts: Int) {
        saveInt(TOTAL_ATTEMPTS, totalAttempts)
        this.totalAttempts.intValue = totalAttempts
    }

    private fun getVoyelles() : List<Char> {
        return when {
            dictionary.value.startsWith("fr") -> voyellesFr
            else -> voyellesEn
        }
    }

    private fun getInt(key: String, default : Int) : Int {
        return sharedPreferences.getInt(key, default)
    }

    private fun saveInt(key : String, value : Int?) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(key)
        } else {
            editor.putInt(key, value)
        }
        editor.apply()
    }

    private fun getString(key: String, default : String) : String {
        return sharedPreferences.getString(key, default) ?: default
    }

    private fun saveString(key : String, value : String?) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(key)
        } else {
            editor.putString(key, value)
        }
        editor.apply()
    }

    companion object {
        const val DICTIONARY = "dictionary"
        const val MIN_LENGTH = "min_length"
        const val MAX_LENGTH = "max_length"
        const val FREE_LETTERS = "free_letters"
        const val DISCARD_LETTERS = "discard_letters"
        const val TOTAL_ATTEMPTS = "errors_allowed"

        private val voyellesFr = listOf('A', 'E', 'I', 'O', 'U')
        private val voyellesEn = listOf('A', 'E', 'I', 'O', 'U', 'Y')

        val dictionaries = mapOf(
            "fr_xs" to "\uD83C\uDDEB\uD83C\uDDF7   Français - XS",
            "fr_m" to "\uD83C\uDDEB\uD83C\uDDF7   Français - M",
            "fr_l" to "\uD83C\uDDEB\uD83C\uDDF7   Français - L",
            "fr_xxl" to "\uD83C\uDDEB\uD83C\uDDF7   Français - XXL",
            "en_xs" to "\uD83C\uDDEC\uD83C\uDDE7   English - XS",
            "en_s" to "\uD83C\uDDEC\uD83C\uDDE7   English - S",
            "en_l" to "\uD83C\uDDEC\uD83C\uDDE7   English - L"
        )
    }
}