package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.Trek

@Database(
    entities = [Trek::class],
    version = 3,
    exportSchema = false
)
abstract class TrekDatabase : RoomDatabase() {

    abstract fun trekDao(): TrekDao

    companion object {
        @Volatile
        private var INSTANCE: TrekDatabase? = null

        fun getDatabase(context: Context): TrekDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrekDatabase::class.java,
                    "arolock_treks_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
