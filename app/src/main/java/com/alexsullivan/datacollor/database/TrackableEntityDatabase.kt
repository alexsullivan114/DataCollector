package com.alexsullivan.datacollor.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@Database(
    version = 5,
    entities = [BooleanTrackableEntity::class, NumberTrackableEntity::class, Trackable::class],
    autoMigrations = [
        AutoMigration(from = 4, to = 5)
    ]
)
@TypeConverters(Converters::class)
abstract class TrackableEntityDatabase : RoomDatabase() {

    class ThreeToFourManualMigration: Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
           val renameTable = """
               ALTER TABLE trackable_entity_table
               RENAME TO boolean_trackable_entity_table;
           """.trimIndent()
            database.execSQL(renameTable)
        }
    }

    abstract fun trackableDao(): TrackableDao
    abstract fun trackableBooleanDao(): BooleanEntityDao
    abstract fun trackableNumberDao(): NumberEntityDao

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
                    .addMigrations(ThreeToFourManualMigration())
                    .addCallback(object: RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            println("Database created")
                            val initialTrackables = generateDefaultTrackables()
                            val dao = getDatabase(context).trackableDao()
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

        private fun generateDefaultTrackables(): List<Trackable> = listOf(
            Trackable(UUID.randomUUID().toString(), "Coffee past 12", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Alcohol", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Slept well", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Woke up rested", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Exercise", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Meditate", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Morning brush", false, TrackableType.BOOLEAN),
            Trackable(UUID.randomUUID().toString(), "Evening brush", false, TrackableType.BOOLEAN),
        )
    }
}
