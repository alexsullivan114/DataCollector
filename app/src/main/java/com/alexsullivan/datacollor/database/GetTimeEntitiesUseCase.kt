package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.TimeEntityDao
import com.alexsullivan.datacollor.database.entities.TimeTrackableEntity
import javax.inject.Inject

class GetTimeEntitiesUseCase @Inject constructor(private val timeEntityDao: TimeEntityDao){
    suspend operator fun invoke(id: String): List<TimeTrackableEntity> {
        return timeEntityDao.getEntities(id)
    }
}
