package com.alexsullivan.datacollor

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
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
        val manager = TrackableManager(TrackableEntityDatabase.getDatabase(this))
        lifecycleScope.launch {
            manager.init()
            val enabledTrackables = manager.getEnabledTrackables()
            val allTrackables = manager.getAllTrackables()
            val enabledTrackableIds = enabledTrackables.map { it.id }
            setContent {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    allTrackables.forEach { trackable ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(trackable.title)
                                val isChecked = enabledTrackableIds.contains(trackable.id)
                                Checkbox(checked = isChecked, onCheckedChange = { checked ->
                                    enableTrackable(trackable, checked)
                                })
                            }
                            Divider()
                        }
                    }
                    item {
                        Button(modifier = Modifier.padding(16.dp).fillMaxWidth(), onClick = { export() }) {
                            Text("Export")
                        }
                    }
                }
            }
        }
    }

    private fun export() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = TrackableEntityDatabase.getDatabase(this@MainActivity).trackableEntityDao()
                val trackables = dao.getTrackableEntities()
                val csvText = trackables.map {
                    val trackable = dao.getTrackableById(it.trackableId)
                    "${trackable.title}, ${it.executed}, ${it.date}"
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

    private fun enableTrackable(trackable: Trackable, checked: Boolean) {
        val manager = TrackableManager(TrackableEntityDatabase.getDatabase(this))
        lifecycleScope.launch {
            manager.toggleTrackableEnabled(trackable, checked)
        }
    }
}
