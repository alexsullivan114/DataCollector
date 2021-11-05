package com.alexsullivan.datacollor

import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager
import java.util.*

class UpdateTrackablesUseCase(private val trackableManager: TrackableManager) {
   suspend fun addTrackable(title: String) {
      val uuid = UUID.randomUUID().toString()
      val trackable = Trackable(uuid, title, true)
      trackableManager.addTrackable(trackable)
   }

   suspend fun deleteTrackable(trackable: Trackable) {
      trackableManager.deleteTrackable(trackable)
      trackableManager.deleteTrackableEntities(trackable.id)
   }
}
