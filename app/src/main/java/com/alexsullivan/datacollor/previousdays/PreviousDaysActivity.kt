package com.alexsullivan.datacollor.previousdays

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.RatingView
import com.alexsullivan.datacollor.previousdays.DisplayableTrackableEntity.*
import com.alexsullivan.datacollor.utils.displayableString
import com.alexsullivan.datacollor.utils.refreshWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
data class TimeEntityState(val timePickerState: TimePickerState, val entity: TimeEntity)

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class PreviousDaysActivity : AppCompatActivity() {
    private val viewModel: PreviousDaysViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var datePickerState by remember { mutableStateOf<DatePickerState?>(null) }
                Scaffold(
                    topBar = { QLAppBar(onDateSelected = {
                       datePickerState = DatePickerState(
                           initialDisplayedMonthMillis = null,
                           initialSelectedDateMillis = null,
                           yearRange = 1900..LocalDate.now().year,
                           initialDisplayMode = DisplayMode.Picker
                       )
                    }) }
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
                                    text = getString(android.R.string.ok),
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

        lifecycleScope.launch {
            viewModel.triggerUpdateWidgetFlow.collect {
                refreshWidget(this@PreviousDaysActivity)
            }
        }
    }

    @Composable
    private fun QLAppBar(onDateSelected: () -> Unit) {
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

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    private fun TrackableEntitiesList(modifier: Modifier = Modifier, onTimeEntityClicked: (TimeEntity) -> Unit) {
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
    private fun EntityView(entity: DisplayableTrackableEntity, onTimeEntityClicked: (TimeEntity) -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = entity.title,
                style = TextStyle(fontSize = 14.sp)
            )
            when (entity) {
                is BooleanEntity -> BooleanEntityView(entity)
                is NumberEntity -> NumberEntityView(entity)
                is RatingEntity -> RatingEntityView(entity)
                is TimeEntity -> TimeEntityView(entity, onTimeEntityClicked)
            }
        }
    }

    @Composable
    private fun BooleanEntityView(entity: BooleanEntity) {
        Checkbox(checked = entity.checked, onCheckedChange = {
            viewModel.booleanEntityChanged(entity)
        })
    }

    @Composable
    private fun NumberEntityView(entity: NumberEntity) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.numberEntityChanged(false, entity) }) {
                Icon(painterResource(id = R.drawable.minus), "Minus")
            }
            Text(entity.count.toString())
            IconButton(onClick = { viewModel.numberEntityChanged(true, entity) }) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
    }

    @Composable
    private fun RatingEntityView(entity: RatingEntity) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.ratingEntityChanged(false, entity) }) {
                Icon(painterResource(id = R.drawable.minus), "Minus")
            }
            RatingView(entity.rating, Modifier.size(24.dp))
            IconButton(onClick = { viewModel.ratingEntityChanged(true, entity) }) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
    }

    @Composable
    private fun TimeEntityView(entity: TimeEntity, onTimeEntityClicked: (TimeEntity) -> Unit) {
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
}
