package com.alexsullivan.datacollor.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexsullivan.datacollor.database.daos.BooleanEntityDao
import com.alexsullivan.datacollor.database.daos.NumberEntityDao
import com.alexsullivan.datacollor.database.daos.RatingEntityDao
import com.alexsullivan.datacollor.database.daos.TimeEntityDao
import com.alexsullivan.datacollor.database.daos.TrackableDao
import com.alexsullivan.datacollor.database.daos.WeatherDao
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import com.alexsullivan.datacollor.database.entities.TimeTrackableEntity
import com.alexsullivan.datacollor.database.entities.WeatherEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@Database(
    version = 10,
    entities = [
        BooleanTrackableEntity::class,
        NumberTrackableEntity::class,
        RatingTrackableEntity::class,
        TimeTrackableEntity::class,
        Trackable::class,
        WeatherEntity::class
    ],
    autoMigrations = [
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10)
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
    abstract fun trackableRatingDao(): RatingEntityDao
    abstract fun trackableTimeDao(): TimeEntityDao
    abstract fun weatherDao(): WeatherDao

    companion object {
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
                    .fallbackToDestructiveMigration()
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
