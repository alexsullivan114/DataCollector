package com.alexsullivan.datacollor.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.alexsullivan.datacollor.BuildConfig
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase
import com.alexsullivan.datacollor.serialization.TrackableSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class ExportUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getLifetimeData: GetLifetimeDataUseCase
) {

    suspend fun export() {
        withContext(Dispatchers.IO) {
            val csvText = TrackableSerializer.serialize(getLifetimeData())

            val dir = File(context.filesDir, "csvs")
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
                Log.e("ExportUtil", "Failed to write export file", e)
            }

            if (file != null) {
                val uri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sharingIntent.action = Intent.ACTION_SEND
                sharingIntent.type = "text/csv"
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                context.startActivity(sharingIntent)
            }
        }
    }
}
