package com.alexsullivan.datacollor.configuration
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.CollectorWidget
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.home.AddTrackableDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfigurationActivity : AppCompatActivity() {
    private val viewModel: ConfigurationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackableList()
        }

        lifecycleScope.launch {
            viewModel.triggerUpdateWidgetFlow
                .collect {
                    refreshWidget()
                }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TrackableList(modifier: Modifier = Modifier) {
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
                    modifier = modifier
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
                        DoneButton()
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
    fun DoneButton(modifier: Modifier = Modifier) {
        Button(modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(), onClick = {
            val appWidgetId = intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            refreshWidget()
            finish()
        }) {
            Text("Done")
        }
    }

    @Composable
    fun TrackableItem(trackable: Trackable, modifier: Modifier = Modifier) {
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

    private fun refreshWidget() {
        val intent = Intent(this@ConfigurationActivity, CollectorWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(application)
            .getAppWidgetIds(ComponentName(application, CollectorWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}
