package com.alexsullivan.datacollor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(entities = [TrackableEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TrackableEntityDatabase: RoomDatabase() {
    abstract fun trackableEntityDao(): TrackableEntityDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TrackableEntityDatabase? = null

        fun getDatabase(context: Context): TrackableEntityDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackableEntityDatabase::class.java,
                    "word_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}