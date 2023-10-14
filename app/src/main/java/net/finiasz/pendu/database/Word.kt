package net.finiasz.pendu.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import net.finiasz.pendu.unAccent


@Entity(
    tableName = "word",
    indices = [
        Index("longueur"),
        Index("fr_xs"),
        Index("fr_m"),
        Index("fr_l"),
        Index("fr_xxl"),
        Index("en_xs"),
        Index("en_s"),
        Index("en_l"),
    ],
)
data class Word(
    @PrimaryKey
    val mot : String,
    val longueur : Int,
    val fr_xs : Boolean,
    val fr_m : Boolean,
    val fr_l : Boolean,
    val fr_xxl : Boolean,
    val en_xs : Boolean,
    val en_s : Boolean,
    val en_l : Boolean,
)
