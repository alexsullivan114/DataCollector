package com.alexsullivan.datacollor

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class ExportUtil(private val activity: Activity) {
    suspend fun export() {
        withContext(Dispatchers.IO) {
            val dao = TrackableEntityDatabase.getDatabase(activity).trackableEntityDao()
            val trackables = dao.getTrackableEntities()
            val csvText = trackables.map {
                val trackable = dao.getTrackableById(it.trackableId)
                "${trackable.title}, ${it.executed}, ${it.date}"
            }.fold("") { acc, entity ->
                acc + entity + "\n"
            }

            val dir = File(activity.filesDir, "csvs")
            if (!dir.exists()) {
                dir.mkdir()
            }


            var file: File? = null
            try {
                file = File(dir, "export.csv")
                val writer = FileWriter(file)
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
                activity.startActivity(Intent.createChooser(sharingIntent, "Do it to it"))
            }
        }
    }
}
