package com.alexsullivan.datacollor.drive

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alexsullivan.datacollor.QLPreferences
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager

class DriveUploadWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val trackableEntityDatabase = TrackableEntityDatabase.getDatabase(context)
        val trackableManager = TrackableManager(trackableEntityDatabase)
        val backupTrackablesUseCase = BackupTrackablesUseCase(trackableManager, context)
        val prefs = QLPreferences(context)
        prefs.backupFileId?.let {
            backupTrackablesUseCase.uploadToDrive(it)
        }
        return Result.success()
    }
}
