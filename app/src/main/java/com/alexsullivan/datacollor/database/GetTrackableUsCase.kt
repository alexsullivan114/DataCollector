package com.alexsullivan.datacollor.database

import com.alexsullivan.datacollor.database.daos.TrackableDao
import javax.inject.Inject

class GetTrackableUsCase @Inject constructor(private val dao: TrackableDao){
    suspend operator fun invoke(id: String): Trackable? {
       return dao.getTrackableById(id)
    }
}
