package com.alexsullivan.datacollor.drive

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alexsullivan.datacollor.QLPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DriveUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val backupTrackablesUseCase: BackupTrackablesUseCase,
    private val preferences: QLPreferences
) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        preferences.backupFileId?.let {
            backupTrackablesUseCase.uploadToDrive(it)
        }
        return Result.success()
    }
}
