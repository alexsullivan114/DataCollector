package com.alexsullivan.datacollor

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class ExportUtil(private val activity: Activity) {

    suspend fun export() {
        withContext(Dispatchers.IO) {
            val database = TrackableEntityDatabase.getDatabase(activity)
            val trackableManager = TrackableManager(database)
            val trackableEntities = trackableManager.getTrackableEntities()
            val trackables = trackableManager.getTrackables()
            Log.d("Export", "Trackables: $trackables")
            val csvText = TrackableSerializer.serialize(trackableEntities, trackables)

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
