package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.BooleanEntityDao
import com.alexsullivan.datacollor.database.daos.NumberEntityDao
import com.alexsullivan.datacollor.database.daos.RatingEntityDao
import com.alexsullivan.datacollor.database.daos.TimeEntityDao
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import javax.inject.Inject

class GetTrackableEntitiesUseCase @Inject constructor(
    private val booleanEntityDao: BooleanEntityDao,
    private val numberEntityDao: NumberEntityDao,
    private val ratingEntityDao: RatingEntityDao,
    private val timeEntityDao: TimeEntityDao
) {
    suspend operator fun invoke(): List<TrackableEntity> {
        val booleansEntities = booleanEntityDao.getEntities().map {
            TrackableEntity.Boolean(it)
        }
        val numberEntities = numberEntityDao.getEntities().map {
            TrackableEntity.Number(it)
        }
        val ratingEntities = ratingEntityDao.getEntities().map {
            TrackableEntity.Rating(it)
        }
        val timeEntities = timeEntityDao.getEntities().map {
            TrackableEntity.Time(it)
        }

        return booleansEntities + numberEntities + ratingEntities + timeEntities
    }
}
