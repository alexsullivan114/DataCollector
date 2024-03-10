@file:OptIn(ExperimentalMaterial3Api::class)

package com.alexsullivan.datacollor.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexsullivan.datacollor.AddTrackableDialog
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.DeleteTrackableDialog
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.Trackable

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: (String) -> Unit,
    onNavigateToPreviousDays: () -> Unit
) {
    val showAddDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf<Trackable?>(null) }
    val showBottomSheet = remember { mutableStateOf<Trackable?>(null) }
    AppTheme {
        Scaffold(
            topBar = { QLAppBar(onNavigateToSettings, onNavigateToPreviousDays) },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog.value = true }) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }
        ) {
            TrackableItemList(
                modifier = Modifier.padding(it),
                showAddDialog,
                showDeleteDialog,
                showBottomSheet,
                onNavigateToInsights
            )
        }

    }
}

@Composable
fun TrackableItemList(
    modifier: Modifier,
    showAddDialog: MutableState<Boolean>,
    showDeleteDialog: MutableState<Trackable?>,
    showBottomSheet: MutableState<Trackable?>,
    onTrackableClicked: (String) -> Unit
) {
    val viewModel: MainViewModel = hiltViewModel()
    val trackables by viewModel.itemsFlow.collectAsState()
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Toggle to add tracking to the widget",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        trackables.sortedBy { it.title }.forEach { trackable ->
            item(key = trackable.id) {
                TrackableItem(trackable, modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        showDeleteDialog.value = trackable
                    }, onTap = {
                        onTrackableClicked(trackable.id)
                    })
                }, onOptionsSelected = { showBottomSheet.value = trackable })
            }
            // Update our bottom sheet value if we're currently showing it.
            if (trackable.id == showBottomSheet.value?.id) {
                showBottomSheet.value = trackable
            }
        }
        item {
            ExportButton()
        }
    }
    if (showAddDialog.value) {
        AddTrackableDialog(
            onDismiss = { showAddDialog.value = false },
            onDone = {
                showAddDialog.value = false
                viewModel.trackableAdded(it)
            }
        )
    }
    showDeleteDialog.value?.let { trackable ->
        DeleteTrackableDialog(onDelete = {
            showDeleteDialog.value = null
            viewModel.trackableDeleted(it)
        }, onCancel = {
            showDeleteDialog.value = null
        }, trackable = trackable)
    }
    showBottomSheet.value?.let { trackable ->
        OptionsBottomSheet(
            trackable = trackable,
            onDismiss = { showBottomSheet.value = null }
        ) {
            showBottomSheet.value = null
            showDeleteDialog.value = trackable
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QLAppBar(onNavigateToSettings: () -> Unit, onNavigateToPreviousDays: () -> Unit) {
    var showOptionsDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = { showOptionsDropdown = true }) {
                Icon(Icons.Filled.MoreVert, "Menu")
            }
            DropdownMenu(
                expanded = showOptionsDropdown,
                onDismissRequest = { showOptionsDropdown = false }) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        showOptionsDropdown = false
                        onNavigateToSettings()
                    })
                DropdownMenuItem(
                    text = { Text("Past Days") },
                    onClick = {
                        showOptionsDropdown = false
                        onNavigateToPreviousDays()
                    })
            }
        }
    )
}

@Composable
fun TrackableItem(
    trackable: Trackable,
    modifier: Modifier = Modifier,
    onOptionsSelected: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(trackable.title)
        Icon(
            painter = painterResource(R.drawable.more_horiz),
            "options",
            modifier = Modifier.clickable(onClick = onOptionsSelected)
        )
    }
    Divider()
}

@Composable
fun ExportButton(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = hiltViewModel()
    Button(modifier = modifier
        .padding(16.dp)
        .fillMaxWidth(), onClick = viewModel::export
    ) {
        Text("Export")
    }
}

@Composable
fun OptionsBottomSheet(trackable: Trackable, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val viewModel: MainViewModel = hiltViewModel()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enabled", style = MaterialTheme.typography.titleLarge)
                Checkbox(
                    checked = trackable.enabled,
                    onCheckedChange = { viewModel.trackableToggled(trackable, it) }
                )
            }
            Text(
                text = "Delete",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.Red),
                modifier = Modifier.clickable(onClick = onDelete)
            )
        }
    }
}
