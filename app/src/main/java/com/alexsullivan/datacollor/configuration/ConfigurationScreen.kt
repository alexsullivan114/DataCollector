package com.alexsullivan.datacollor.configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexsullivan.datacollor.AddTrackableDialog
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.database.Trackable

@Composable
fun ConfigurationScreen(onDoneClicked: () -> Unit) {
   TrackableList(onDoneClicked)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackableList(onDoneClicked: () -> Unit) {
    val viewModel = hiltViewModel<ConfigurationViewModel>()
    var showDialog by remember { mutableStateOf(false) }
    val trackables by viewModel.itemsFlow.collectAsState()
    AppTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it),
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
                        val dismissState = rememberDismissState()
                        val isDismissed =
                            dismissState.isDismissed(DismissDirection.EndToStart) ||
                                    dismissState.isDismissed(DismissDirection.StartToEnd)
                        if (isDismissed) {
                            viewModel.trackableDeleted(trackable)
                        }
                        SwipeToDismiss(
                            state = dismissState,
                            background = { Box(modifier = Modifier.background(Color.Red)) },
                            dismissContent = {
                                TrackableItem(trackable)
                            })
                    }
                }
                item {
                    DoneButton(onDoneClicked)
                }
            }
            if (showDialog) {
                AddTrackableDialog(
                    onDismiss = { showDialog = false },
                    onDone = {
                        showDialog = false
                        viewModel.trackableAdded(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun DoneButton(onDoneClicked: () -> Unit) {
    Button(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(), onClick = {
        onDoneClicked()
    }) {
        Text("Done")
    }
}

@Composable
private fun TrackableItem(trackable: Trackable, modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<ConfigurationViewModel>()
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
