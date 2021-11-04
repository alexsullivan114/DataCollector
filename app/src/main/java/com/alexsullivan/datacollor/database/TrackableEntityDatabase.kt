package com.alexsullivan.datacollor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alexsullivan.datacollor.Converters

@Database(entities = [TrackableEntity::class, Trackable::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TrackableEntityDatabase: RoomDatabase() {
    abstract fun trackableEntityDao(): TrackableDao

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
                    "sqlite.db"
                )
                    .createFromAsset("sqlite.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
