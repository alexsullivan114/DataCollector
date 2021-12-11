package com.alexsullivan.datacollor.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexsullivan.datacollor.Converters
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Database(entities = [TrackableEntity::class, Trackable::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TrackableEntityDatabase: RoomDatabase() {
    abstract fun trackableEntityDao(): TrackableDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TrackableEntityDatabase? = null

        @DelicateCoroutinesApi
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
                    .addCallback(object: RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            println("Database created")
                            val initialTrackables = listOf(
                                Trackable(UUID.randomUUID().toString(), "Coffee past 12", false),
                                Trackable(UUID.randomUUID().toString(), "Alcohol", false),
                                Trackable(UUID.randomUUID().toString(), "Slept well", false),
                                Trackable(UUID.randomUUID().toString(), "Woke up rested", false),
                                Trackable(UUID.randomUUID().toString(), "Exercise", false),
                                Trackable(UUID.randomUUID().toString(), "Meditate", false),
                                Trackable(UUID.randomUUID().toString(), "Morning brush", false),
                                Trackable(UUID.randomUUID().toString(), "Evening brush", false),
                            )
                            val dao = getDatabase(context).trackableEntityDao()
                            GlobalScope.launch {
                                initialTrackables.forEach { dao.saveTrackable(it) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
