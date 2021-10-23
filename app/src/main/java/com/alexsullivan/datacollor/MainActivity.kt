package com.alexsullivan.datacollor

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter


class MainActivity : AppCompatActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { _: Boolean ->

        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { export() }) {
                    Text("Export")
                }
            }
        }
    }

    private fun export() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val trackables =
                    TrackableEntityDatabase.getDatabase(this@MainActivity).trackableEntityDao()
                        .getTrackableEntities()
                val csvText = trackables.map {
                    "${it.trackable.name}, ${it.executed}, ${it.date}"
                }.fold("") { acc, entity ->
                   acc + entity + "\n"
                }

                val dir = File(filesDir, "csvs")
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
                        this@MainActivity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sharingIntent.action = Intent.ACTION_SEND
                    sharingIntent.type = "text/csv"
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(sharingIntent, "Do it to it"))
                }
            }
        }
    }
}
