package com.alexsullivan.datacollor.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import java.util.*

@Composable
fun AddTrackableDialog(onDismiss: () -> Unit, onDone: (Trackable) -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TrackableType.BOOLEAN) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(modifier = Modifier.padding(16.dp), text = "Add item to track")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        modifier = Modifier.padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        value = text,
                        onValueChange = { text = it })
                }
                TrackableTypeOptions(
                    onTypeSelected = { selectedType = it },
                    selectedType = selectedType
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        val trackable = Trackable(UUID.randomUUID().toString(), text, true, selectedType)
                        onDone(trackable)
                    }) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackableTypeOptions(
    selectedType: TrackableType,
    onTypeSelected: (TrackableType) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        TrackableType.values().forEach { trackableType ->
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .clickable { onTypeSelected(trackableType) }) {
                val typeTitle = trackableType.name.lowercase().replaceFirstChar {
                    it.titlecase(Locale.getDefault())
                }
                RadioButton(
                    selected = selectedType == trackableType,
                    onClick = { onTypeSelected(trackableType) })
                Text(typeTitle, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
