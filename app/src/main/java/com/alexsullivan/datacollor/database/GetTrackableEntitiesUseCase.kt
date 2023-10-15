package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.BooleanEntityDao
import com.alexsullivan.datacollor.database.daos.NumberEntityDao
import com.alexsullivan.datacollor.database.daos.RatingEntityDao
import com.alexsullivan.datacollor.database.entities.TrackableEntity
import javax.inject.Inject

class GetTrackableEntitiesUseCase @Inject constructor(
    private val booleanEntityDao: BooleanEntityDao,
    private val numberEntityDao: NumberEntityDao,
    private val ratingEntityDao: RatingEntityDao
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

        return booleansEntities + numberEntities + ratingEntities
    }
}
