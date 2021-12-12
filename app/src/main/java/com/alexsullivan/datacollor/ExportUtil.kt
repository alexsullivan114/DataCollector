package com.alexsullivan.datacollor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.Scopes
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat

class ExportUtil(private val activity: Activity) {

    suspend fun export() {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        withContext(Dispatchers.IO) {
            val dao = TrackableEntityDatabase.getDatabase(activity).trackableEntityDao()
            val trackableEntities = dao.getTrackableEntities()
            val trackables = dao.getTrackables()
            Log.d("Export", "Trackables: $trackables")
            val groupedTrackables = trackableEntities.groupBy { it.date }.toSortedMap()
            val csvText = groupedTrackables.map { (date, entities) ->
                var entry = format.format(date)
                entities.forEach { entity ->
                    Log.d("Export", "Fetching trackable for id ${entity.trackableId}")
                    val trackable = trackables.first { it.id == entity.trackableId }
                    entry += ",${trackable.title},${entity.executed}"
                }
                entry
            }.fold("") { acc, entity -> acc + entity + "\n" }

            val dir = File(activity.filesDir, "csvs")
            if (!dir.exists()) {
                dir.mkdir()
            }

            var file: File? = null
            try {
                file = File(dir, "quantified_life_export.csv")
                val writer = FileWriter(file)
                print(csvText)
                writer.append(csvText)
                writer.flush()
                writer.close()
            } catch (e: Exception) {

            }

            if (file != null) {
                val uri = FileProvider.getUriForFile(
                    activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.action = Intent.ACTION_SEND
                sharingIntent.type = "text/csv"
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                activity.startActivity(sharingIntent)
            }
        }
    }
}
