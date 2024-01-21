package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.NumberEntityDao
import com.alexsullivan.datacollor.database.entities.NumberTrackableEntity
import javax.inject.Inject

class GetNumberEntitiesUseCase @Inject constructor(private val numberEntityDao: NumberEntityDao){
    suspend operator fun invoke(id: String): List<NumberTrackableEntity> {
        return numberEntityDao.getEntities(id)
    }
}
