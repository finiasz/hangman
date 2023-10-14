package net.finiasz.pendu.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WordDao {
    @Insert
    fun insert(word: Word)


    // region count

    @Query("SELECT COUNT(*) FROM word WHERE fr_xs = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInFrXs(min: Int, max: Int) : Int

    @Query("SELECT COUNT(*) FROM word WHERE fr_m = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInFrM(min: Int, max: Int) : Int

    @Query("SELECT COUNT(*) FROM word WHERE fr_l = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInFrL(min: Int, max: Int) : Int

    @Query("SELECT COUNT(*) FROM word WHERE fr_xxl = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInFrXxl(min: Int, max: Int) : Int

    @Query("SELECT COUNT(*) FROM word WHERE en_xs = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInEnXs(min: Int, max: Int) : Int

    @Query("SELECT COUNT(*) FROM word WHERE en_s = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInEnS(min: Int, max: Int) : Int

    @Query("SELECT COUNT(*) FROM word WHERE en_l = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur)")
    fun countInEnL(min: Int, max: Int) : Int

    // endregion

    // region pick a word
    @Query("SELECT * FROM word WHERE fr_xs = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInFrXs(num: Int, min: Int, max: Int) : Word

    @Query("SELECT * FROM word WHERE fr_m = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInFrM(num: Int, min: Int, max: Int) : Word

    @Query("SELECT * FROM word WHERE fr_l = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInFrL(num: Int, min: Int, max: Int) : Word

    @Query("SELECT * FROM word WHERE fr_xxl = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInFrXxl(num: Int, min: Int, max: Int) : Word

    @Query("SELECT * FROM word WHERE en_xs = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInEnXs(num: Int, min: Int, max: Int) : Word

    @Query("SELECT * FROM word WHERE en_s = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInEnS(num: Int, min: Int, max: Int) : Word

    @Query("SELECT * FROM word WHERE en_l = 1 AND (:min = 0 OR :min <= longueur) AND (:max = 0 OR :max >= longueur) LIMIT 1 OFFSET :num")
    fun pickInEnL(num: Int, min: Int, max: Int) : Word

    // endregion


    // region creation of database

    @Query("SELECT * FROM word WHERE mot = :mot")
    fun get(mot: String) : Word?

    @Query("UPDATE word SET fr_xs = 1 WHERE mot = :mot")
    fun markFrXs(mot: String)

    @Query("UPDATE word SET fr_m = 1 WHERE mot = :mot")
    fun markFrM(mot: String)

    @Query("UPDATE word SET fr_l = 1 WHERE mot = :mot")
    fun markFrL(mot: String)

    @Query("UPDATE word SET fr_xxl = 1 WHERE mot = :mot")
    fun markFrXxl(mot: String)

    @Query("UPDATE word SET en_xs = 1 WHERE mot = :mot")
    fun markEnXs(mot: String)

    @Query("UPDATE word SET en_s = 1 WHERE mot = :mot")
    fun markEnS(mot: String)

    @Query("UPDATE word SET en_l = 1 WHERE mot = :mot")
    fun markEnL(mot: String)

    // endregion
}