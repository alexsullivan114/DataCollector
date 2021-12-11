package com.alexsullivan.datacollor

import android.content.Context
import android.util.Log
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class BackupTrackablesUseCase(
    private val trackableEntityDatabase: TrackableEntityDatabase,
    private val context: Context
) {
    suspend fun uploadToDrive(fileId: String) = withContext(Dispatchers.IO) {
        val drive = createDriveObject()
        val newFile = File()
        val newInputStream = createTrackablesCsv().byteInputStream()
        drive.files().update(fileId, newFile, InputStreamContent(null, newInputStream)).execute()
    }

    suspend fun createDriveFile(): String = withContext(Dispatchers.IO) {
        val drive = createDriveObject()
        val newFile = File()
        newFile.name = "quantified_self.csv"
        val newInputStream = createTrackablesCsv().byteInputStream()
        val file = drive.files().create(newFile, InputStreamContent(null, newInputStream)).execute()
        file.id
    }

    private suspend fun createTrackablesCsv() = withContext(Dispatchers.IO) {
        val dao = trackableEntityDatabase.trackableEntityDao()
        val trackableEntities = dao.getTrackableEntities()
        val trackables = dao.getTrackables()
        TrackableSerializer.serialize(trackableEntities, trackables)
    }

    suspend fun fetchExistingDriveFileId(): String? = withContext(Dispatchers.IO) {
        val drive = createDriveObject()
        val fileList = drive.Files().list().setQ("trashed=false").execute()
        if (fileList.files.size > 0) {
            fileList.files[0].id
        } else {
            null
        }
    }

    suspend fun fetchExistingDriveFileContents(id: String): String = withContext(Dispatchers.IO) {
        val drive = createDriveObject()
        val inputStream = drive.files().get(id).executeMediaAsInputStream()
        val csv = inputStream.bufferedReader().use { it.readText() }
        csv
    }

    private fun createDriveObject(): Drive {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(Scopes.DRIVE_FILE))
        credential.selectedAccount = account.account
        val drive = Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
            .setApplicationName("Quantified Life")
            .build()
        return drive
    }
}
