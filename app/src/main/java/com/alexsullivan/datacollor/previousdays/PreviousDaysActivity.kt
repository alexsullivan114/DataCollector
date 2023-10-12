package com.alexsullivan.datacollor.previousdays

//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.RatingView
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.utils.refreshWidget
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.ZoneOffset

class PreviousDaysActivity : AppCompatActivity() {

    private val viewModel: PreviousDaysViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = TrackableEntityDatabase.getDatabase(this@PreviousDaysActivity)
                val manager = TrackableManager(database)
                return PreviousDaysViewModel(manager) as T
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Scaffold(
                    topBar = { QLAppBar() }
                ) {
                    TrackableEntitiesList()
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TrackableEntitiesList() {
        val uiState by viewModel.uiFlow.collectAsState()
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
        ) {
            uiState.items.forEach { entity ->
                item {
                    Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        EntityView(entity)
                    }
                }
            }
        }
    }

    @Composable
    private fun EntityView(entity: DisplayableTrackableEntity) {
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
            }
        }
    }

    @Composable
    private fun BooleanEntityView(entity: DisplayableTrackableEntity.BooleanEntity) {
        Checkbox(checked = entity.checked, onCheckedChange = {
            viewModel.booleanEntityChanged(entity)
        })
    }

    @Composable
    private fun NumberEntityView(entity: DisplayableTrackableEntity.NumberEntity) {
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
    private fun RatingEntityView(entity: DisplayableTrackableEntity.RatingEntity) {
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
