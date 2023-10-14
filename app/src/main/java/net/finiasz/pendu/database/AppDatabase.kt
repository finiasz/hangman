package net.finiasz.pendu.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.finiasz.pendu.App

@Database(
    entities = [ Word::class ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var instance : AppDatabase? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: Room
                .databaseBuilder(App.instance.applicationContext, AppDatabase::class.java, "words.db")
                .createFromAsset("database/words.db")
                .build().also {
                    instance = it
                }
        }
    }
}