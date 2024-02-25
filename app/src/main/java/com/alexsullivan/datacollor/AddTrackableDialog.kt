package com.alexsullivan.datacollor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableType
import java.util.Locale
import java.util.UUID

@Composable
fun AddTrackableDialog(onDismiss: () -> Unit, onDone: (Trackable) -> Unit) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TrackableType.BOOLEAN) }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Add item to track",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                        val trackable =
                            Trackable(UUID.randomUUID().toString(), text, true, selectedType)
                        onDone(trackable)
                    }, enabled = text.isNotEmpty()) {
                        Text("Ok")
                    }
                    TextButton(onClick = {
                        onDismiss()
                    }, enabled = text.isNotEmpty()) {
                        Text("Cancel")
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
                    .clickable { onTypeSelected(trackableType) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val typeTitle = trackableType.name.lowercase().replaceFirstChar {
                    it.titlecase(Locale.getDefault())
                }
                RadioButton(
                    selected = selectedType == trackableType,
                    onClick = { onTypeSelected(trackableType) })
                Text(
                    typeTitle,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
