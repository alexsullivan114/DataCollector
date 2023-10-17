package com.alexsullivan.datacollor.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsullivan.datacollor.*
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.GetTrackableEntitiesUseCase
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.drive.BackupTrackablesUseCase
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings") },
                            navigationIcon = {
                                IconButton(onClick = this::finish) {
                                    Icon(
                                        Icons.Filled.ArrowBack,
                                        "Back"
                                    )
                                }
                            })
                    }
                ) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        item {
                            BackupToDriveSetting()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BackupToDriveSetting() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = viewModel::backupToDriveClicked),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isBackingUp by viewModel.backupLoadingFlow.collectAsState()
            Text("Manually backup to Drive")
            if (isBackingUp) {
                CircularProgressIndicator(modifier = Modifier
                    .height(24.dp)
                    .width(24.dp))
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.backup),
                    "Backup to Drive"
                )
            }
        }
    }
}
