package com.alexsullivan.datacollor

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val manager = TrackableManager(TrackableEntityDatabase.getDatabase(this@MainActivity))
                return MainViewModel(manager) as T
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { _: Boolean ->

        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        lifecycleScope.launch {
            setContent {
                TrackableList()
            }
        }
    }

    @Composable
    fun TrackableList(modifier: Modifier = Modifier) {
        val trackables by viewModel.itemsFlow.collectAsState()
        LazyColumn(modifier = modifier.fillMaxWidth()) {
            trackables.forEach { trackable ->
                item {
                    TrackableItem(trackable)
                }
            }
            item {
                ExportButton()
            }
        }
    }

    @Composable
    fun TrackableItem(trackable: Trackable, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(trackable.title)
            Checkbox(checked = trackable.enabled, onCheckedChange = { checked ->
                viewModel.trackableToggled(trackable, checked)
            })
        }
        Divider()
    }

    @Composable
    fun ExportButton(modifier: Modifier = Modifier) {
        Button(modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(), onClick = { export() }) {
            Text("Export")
        }
    }

    private fun export() {
        lifecycleScope.launchWhenCreated { ExportUtil(this@MainActivity).export() }
    }
}
