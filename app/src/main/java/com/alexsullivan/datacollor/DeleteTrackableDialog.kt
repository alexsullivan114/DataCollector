package com.alexsullivan.datacollor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexsullivan.datacollor.database.Trackable

@Composable
fun DeleteTrackableDialog(
    onDelete: (Trackable) -> Unit,
    onCancel: () -> Unit,
    trackable: Trackable
) {
    AlertDialog(
        onDismissRequest = { onDelete(trackable) },
        confirmButton = {
            Text(
                "Delete",
                color = Color.Red,
                modifier = Modifier
                    .clickable { onDelete(trackable) }
                    .padding(16.dp))
        },
        dismissButton = {
            Text(
                "Cancel",
                modifier = Modifier
                    .clickable(onClick = onCancel)
                    .padding(start = 0.dp, bottom = 16.dp, top = 16.dp)
            )
        },
        title = { Text("Are you sure you want to delete ${trackable.title}?") },
        text = { Text("If you delete ${trackable.title} all of its associated data will be deleted. Are you sure you want to delete it?") })
}
