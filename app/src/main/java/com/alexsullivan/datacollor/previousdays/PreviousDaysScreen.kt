@file:OptIn(ExperimentalMaterial3Api::class)

package com.alexsullivan.datacollor.previousdays

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.RatingView
import com.alexsullivan.datacollor.utils.displayableString
import java.time.LocalDate
import java.time.LocalTime

data class TimeEntityState(val timePickerState: TimePickerState, val entity: DisplayableTrackableEntity.TimeEntity)

@Composable
fun PreviousDaysScreen() {
    val viewModel = hiltViewModel<PreviousDaysViewModel>()
    val context = LocalContext.current
    AppTheme {
        var datePickerState by remember { mutableStateOf<DatePickerState?>(null) }
        Scaffold(
            topBar = {
                QLAppBar(onDateSelected = {
                    datePickerState = DatePickerState(
                        initialDisplayedMonthMillis = null,
                        initialSelectedDateMillis = null,
                        yearRange = 1900..LocalDate.now().year,
                        initialDisplayMode = DisplayMode.Picker
                    )
                })
            }
        ) { paddingValues ->
            var timeEntityState by remember { mutableStateOf<TimeEntityState?>(null) }
            TrackableEntitiesList(Modifier.padding(paddingValues)) {
                val hour = it.time?.hour ?: 0
                val minute = it.time?.minute ?: 0
                val timePickerState = TimePickerState(hour, minute, false)
                timeEntityState = TimeEntityState(timePickerState, it)
            }
            if (timeEntityState != null) {
                TimePickerDialog(onCancel = { timeEntityState = null }, onConfirm = {
                    val localTime = LocalTime.of(
                        timeEntityState!!.timePickerState.hour,
                        timeEntityState!!.timePickerState.minute
                    )
                    viewModel.timeEntityChanged(timeEntityState!!.entity, localTime)
                    timeEntityState = null
                }) {
                    TimePicker(state = timeEntityState!!.timePickerState)
                }
            }
            if (datePickerState != null) {
                DatePickerDialog(
                    onDismissRequest = { datePickerState = null },
                    confirmButton = {
                        Text(
                            text = context.getString(android.R.string.ok),
                            modifier = Modifier
                                .clickable {
                                    datePickerState?.selectedDateMillis?.let(viewModel::dateSelected)
                                    datePickerState = null
                                }
                                .padding(16.dp)
                        )
                    }) {
                    DatePicker(state = datePickerState!!)
                }
            }
        }
    }
}

@Composable
private fun QLAppBar(onDateSelected: () -> Unit) {
    val viewModel = hiltViewModel<PreviousDaysViewModel>()
    val uiState by viewModel.uiFlow.collectAsState()
    TopAppBar(title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::previousDayPressed) {
                Icon(Icons.Filled.ArrowBack, "Previous Day")
            }
            Text(
                uiState.date,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable(onClick = onDateSelected)
            )
            IconButton(onClick = viewModel::nextDayPressed, enabled = !uiState.disableNext) {
                Icon(Icons.Filled.ArrowForward, "Next Day")
            }
        }
    })
}

@Composable
private fun TrackableEntitiesList(
    modifier: Modifier = Modifier,
    onTimeEntityClicked: (DisplayableTrackableEntity.TimeEntity) -> Unit
) {
    val viewModel = hiltViewModel<PreviousDaysViewModel>()
    val uiState by viewModel.uiFlow.collectAsState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
    ) {
        uiState.items.forEach { entity ->
            item {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    EntityView(entity, onTimeEntityClicked)
                }
            }
        }
    }

}

@Composable
private fun EntityView(
    entity: DisplayableTrackableEntity,
    onTimeEntityClicked: (DisplayableTrackableEntity.TimeEntity) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = entity.title,
            style = TextStyle(fontSize = 14.sp)
        )
        when (entity) {
            is DisplayableTrackableEntity.BooleanEntity -> BooleanEntityView(entity)
            is DisplayableTrackableEntity.NumberEntity -> NumberEntityView(entity)
            is DisplayableTrackableEntity.RatingEntity -> RatingEntityView(entity)
            is DisplayableTrackableEntity.TimeEntity -> TimeEntityView(entity, onTimeEntityClicked)
        }
    }
}

@Composable
private fun BooleanEntityView(entity: DisplayableTrackableEntity.BooleanEntity) {
    val viewModel = hiltViewModel<PreviousDaysViewModel>()
    Checkbox(checked = entity.checked, onCheckedChange = {
        viewModel.booleanEntityChanged(entity)
    })
}

@Composable
private fun NumberEntityView(entity: DisplayableTrackableEntity.NumberEntity) {
    val viewModel = hiltViewModel<PreviousDaysViewModel>()
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.numberEntityChanged(false, entity) }) {
            Icon(painterResource(id = com.alexsullivan.datacollor.R.drawable.minus), "Minus")
        }
        Text(entity.count.toString())
        IconButton(onClick = { viewModel.numberEntityChanged(true, entity) }) {
            Icon(Icons.Filled.Add, "Add")
        }
    }
}

@Composable
private fun RatingEntityView(entity: DisplayableTrackableEntity.RatingEntity) {
    val viewModel = hiltViewModel<PreviousDaysViewModel>()
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.ratingEntityChanged(false, entity) }) {
            Icon(painterResource(id = com.alexsullivan.datacollor.R.drawable.minus), "Minus")
        }
        RatingView(entity.rating, Modifier.size(24.dp))
        IconButton(onClick = { viewModel.ratingEntityChanged(true, entity) }) {
            Icon(Icons.Filled.Add, "Add")
        }
    }
}

@Composable
private fun TimeEntityView(
    entity: DisplayableTrackableEntity.TimeEntity,
    onTimeEntityClicked: (DisplayableTrackableEntity.TimeEntity) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            entity.time?.displayableString() ?: "---",
            modifier = Modifier
                .clickable {
                    onTimeEntityClicked(entity)
                }
                .padding(top = 10.dp))
    }
}
