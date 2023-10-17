package com.alexsullivan.datacollor.database

import android.content.Context
import com.alexsullivan.datacollor.database.daos.BooleanEntityDao
import com.alexsullivan.datacollor.database.daos.NumberEntityDao
import com.alexsullivan.datacollor.database.daos.RatingEntityDao
import com.alexsullivan.datacollor.database.daos.TrackableDao
import com.alexsullivan.datacollor.database.daos.WeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun providesTrackableEntityDatabase(@ApplicationContext context: Context): TrackableEntityDatabase {
        return TrackableEntityDatabase.getDatabase(context)
    }

    @Provides
    fun providesWeatherDao(database: TrackableEntityDatabase): WeatherDao {
        return database.weatherDao()
    }

    @Provides
    fun providesBooleanEntityDao(database: TrackableEntityDatabase): BooleanEntityDao {
        return database.trackableBooleanDao()
    }

    @Provides
    fun providesNumberEntityDao(database: TrackableEntityDatabase): NumberEntityDao {
        return database.trackableNumberDao()
    }

    @Provides
    fun providesRatingEntityDao(database: TrackableEntityDatabase): RatingEntityDao {
        return database.trackableRatingDao()
    }

    @Provides
    fun providesTrackablesDao(database: TrackableEntityDatabase): TrackableDao {
        return database.trackableDao()
    }

    @Provides
    fun providesTrackableManager(database: TrackableEntityDatabase): TrackableManager {
        return TrackableManager(database)
    }
}
