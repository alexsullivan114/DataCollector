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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
data class TimeEntityState(val timePickerState: TimePickerState, val entity: TimeEntity)

@AndroidEntryPoint
class PreviousDaysActivity : AppCompatActivity() {
    private val viewModel: PreviousDaysViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Scaffold(
                    topBar = { QLAppBar() }
                ) { paddingValues ->
                    var woofers by remember { mutableStateOf<TimeEntityState?>(null) }
                    TrackableEntitiesList(Modifier.padding(paddingValues)) {
                        val hour = it.time?.hour ?: 0
                        val minute = it.time?.minute ?: 0
                        val timePickerState = TimePickerState(hour, minute, false)
                        woofers = TimeEntityState(timePickerState, it)
                    }
                    if (woofers != null) {
                        TimePickerDialog(onCancel = { woofers = null }, onConfirm = {
                            val localTime = LocalTime.of(woofers!!.timePickerState.hour, woofers!!.timePickerState.minute)
                            viewModel.timeEntityChanged(woofers!!.entity, localTime)
                            woofers = null
                        }) {
                            TimePicker(state = woofers!!.timePickerState)
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
    private fun QLAppBar() {
        val uiState by viewModel.uiFlow.collectAsState()
        TopAppBar {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousDayPressed) {
                    Icon(Icons.Filled.ArrowBack, "Previous Day")
                }
                Text(
                    uiState.date,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.clickable { showDatePicker() })
                IconButton(onClick = viewModel::nextDayPressed, enabled = !uiState.disableNext) {
                    Icon(Icons.Filled.ArrowForward, "Next Day")
                }
            }
        }
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

    private fun showDatePicker() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .setOpenAt(viewModel.getDate().atTime(0,0).toInstant(ZoneOffset.UTC).toEpochMilli())
            .build()
        val dialog = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(calendarConstraints)
            .build()
        dialog.addOnPositiveButtonClickListener {
            viewModel.dateSelected(it)
        }
        dialog.show(supportFragmentManager, "DatePicker")
    }
}
