package com.alexsullivan.datacollor.drive

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.database.GetTrackableEntitiesUseCase
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase

class DriveUploadWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val database = TrackableEntityDatabase.getDatabase(context)
        val getTrackableEntities = GetTrackableEntitiesUseCase(
            database.trackableBooleanDao(),
            database.trackableNumberDao(),
            database.trackableRatingDao()
        )
        val getLifetimeData = GetLifetimeDataUseCase(
            database.trackableDao(),
            getTrackableEntities,
            database.weatherDao()
        )
        val backupTrackablesUseCase =
            BackupTrackablesUseCase(context, getLifetimeData)
        val prefs = QLPreferences(context)
        prefs.backupFileId?.let {
            backupTrackablesUseCase.uploadToDrive(it)
        }
        return Result.success()
    }
}
