package com.alexsullivan.datacollor.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.alexsullivan.datacollor.*
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.GetTrackableEntitiesUseCase
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.drive.BackupTrackablesUseCase
import com.alexsullivan.datacollor.drive.DriveUploadWorker
import com.alexsullivan.datacollor.insights.InsightsActivity
import com.alexsullivan.datacollor.previousdays.PreviousDaysActivity
import com.alexsullivan.datacollor.serialization.GetLifetimeDataUseCase
import com.alexsullivan.datacollor.settings.SettingsActivity
import com.alexsullivan.datacollor.utils.ExportUtil
import com.alexsullivan.datacollor.utils.refreshWidget
import com.alexsullivan.datacollor.weather.WeatherWorker
import com.alexsullivan.datacollor.weather.location_import.TakeoutDataManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalMaterialApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var exportUtil: ExportUtil
    @Inject lateinit var takeoutDataManager: TakeoutDataManager

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            GoogleSignIn.getSignedInAccountFromIntent(it.data)
                .addOnSuccessListener {
                    viewModel.signedInToGoogle()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Something went wrong while signing in to Google. Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            registerPeriodicWeatherWorker()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Screen()
        }

        lifecycleScope.launch {
            viewModel.triggerUpdateWidgetFlow.collect {
                refreshWidget(this@MainActivity)
            }
        }
        lifecycleScope.launch {
            viewModel.triggerPeriodicWorkFlow.collect {
                registerPeriodicUploadWorker()
            }
        }

        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        signInToGoogle()
    }

    @Composable
    fun Screen(modifier: Modifier = Modifier) {
        val showAddDialog = remember { mutableStateOf(false) }
        val showDeleteDialog = remember { mutableStateOf<Trackable?>(null) }
        AppTheme {
            Scaffold(
                topBar = { QLAppBar() },
                floatingActionButton = {
                    FloatingActionButton(onClick = { showAddDialog.value = true }) {
                        Icon(Icons.Filled.Add, "Add")
                    }
                }
            ) {
                TrackableItemList(modifier = modifier, showAddDialog, showDeleteDialog)
            }

        }
    }

    @Composable
    fun TrackableItemList(
        modifier: Modifier,
        showAddDialog: MutableState<Boolean>,
        showDeleteDialog: MutableState<Trackable?>
    ) {
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
                            startActivity(
                                InsightsActivity.getIntent(
                                    trackable.id,
                                    this@MainActivity
                                )
                            )
                        })
                    })
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
    }

    @Composable
    fun QLAppBar() {
        var showOptionsDropdown by remember { mutableStateOf(false) }
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                IconButton(onClick = { showOptionsDropdown = true }) {
                    Icon(Icons.Filled.MoreVert, "Menu")
                }
                DropdownMenu(
                    expanded = showOptionsDropdown,
                    onDismissRequest = { showOptionsDropdown = false }) {
                    DropdownMenuItem(onClick = {
                        showOptionsDropdown = false
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }) {
                        Text("Settings")
                    }
                    DropdownMenuItem(onClick = {
                        showOptionsDropdown = false
                        startActivity(Intent(this@MainActivity, PreviousDaysActivity::class.java))
                    }) {
                        Text("Past Days")
                    }
                }
            }
        )
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

    @Composable
    fun ExportButton(modifier: Modifier = Modifier) {
        Button(modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(), onClick = this::export
        ) {
            Text("Export")
        }
    }

    private fun export() {
        lifecycleScope.launchWhenCreated { exportUtil.export() }
    }

    private fun signInToGoogle() {
        val signInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(this, signInOptions)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun registerPeriodicUploadWorker() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<DriveUploadWorker>(
            24,
            TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()
        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
            "UploadToDrive",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun registerPeriodicWeatherWorker() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(
            24, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "Weather",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}
