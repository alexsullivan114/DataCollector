package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.RatingEntityDao
import com.alexsullivan.datacollor.database.entities.RatingTrackableEntity
import javax.inject.Inject

class GetRatingEntitiesUseCase @Inject constructor(private val ratingEntityDao: RatingEntityDao){
    suspend operator fun invoke(id: String): List<RatingTrackableEntity> {
        return ratingEntityDao.getEntities(id)
    }
}
