package com.alexsullivan.datacollor.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                "Back"
                            )
                        }
                    })
            }
        ) {
            LazyColumn(modifier = Modifier
                .padding(it)
                .padding(16.dp)) {
                item {
                    BackupToDriveSetting()
                }
            }
        }
    }
}

@Composable
fun BackupToDriveSetting() {
    val viewModel = hiltViewModel<SettingsViewModel>()
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
