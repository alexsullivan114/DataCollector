package com.alexsullivan.datacollor

import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableManager

class UpdateTrackablesUseCase(private val trackableManager: TrackableManager) {
   suspend fun addTrackable(trackable: Trackable) {
      trackableManager.addTrackable(trackable)
   }

   suspend fun deleteTrackable(trackable: Trackable) {
      trackableManager.deleteTrackable(trackable)
      trackableManager.deleteTrackableEntities(trackable.id)
   }
}
