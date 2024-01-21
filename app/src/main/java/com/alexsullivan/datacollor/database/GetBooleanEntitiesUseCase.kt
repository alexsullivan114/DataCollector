package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.BooleanEntityDao
import com.alexsullivan.datacollor.database.entities.BooleanTrackableEntity
import javax.inject.Inject

class GetBooleanEntitiesUseCase @Inject constructor(private val booleanEntityDao: BooleanEntityDao){
    suspend operator fun invoke(id: String): List<BooleanTrackableEntity> {
        return booleanEntityDao.getEntities(id)
    }
}
