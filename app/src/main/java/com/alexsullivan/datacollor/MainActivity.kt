package com.alexsullivan.datacollor

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.sp
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit

@ExperimentalMaterialApi
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val database = TrackableEntityDatabase.getDatabase(this@MainActivity)
                val manager = TrackableManager(database)
                val updateUseCase = UpdateTrackablesUseCase(manager)
                val backupUseCase = BackupTrackablesUseCase(database, this@MainActivity)
                val prefs = QLPreferences(this@MainActivity)
                return MainViewModel(manager, updateUseCase, backupUseCase, prefs) as T
            }
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            GoogleSignIn.getSignedInAccountFromIntent(it.data)
                .addOnCompleteListener {
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
        lifecycleScope.launch {
            viewModel.triggerPeriodicWorkFlow
                .collect {
                    registerPeriodicUploadWorker()
                }
        }

        signInToGoogle()
    }

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
                            val dismissState = rememberDismissState()
                            val isDismissed =
                                dismissState.isDismissed(DismissDirection.EndToStart) ||
                                        dismissState.isDismissed(DismissDirection.StartToEnd)
                            if (isDismissed) {
                                viewModel.trackableDeleted(trackable)
                            }
                            SwipeToDismiss(
                                state = dismissState,
                                background = { Box(modifier = Modifier.background(Color.Red)) }) {
                                TrackableItem(trackable)
                            }
                        }
                    }
                    item {
                        ExportButton()
                    }
                }
                if (showDialog) {
                    AddItemDialog(
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
    fun AddItemDialog(onDismiss: () -> Unit, onDone: (String) -> Unit) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add item to track") },
            text = {
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    value = text,
                    onValueChange = { text = it })
            },
            confirmButton = { TextButton(onClick = { onDone(text) }) { Text("Ok") } },
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
        lifecycleScope.launchWhenCreated { ExportUtil(this@MainActivity).export() }
    }

    private fun refreshWidget() {
        val intent = Intent(this@MainActivity, CollectorWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(application)
            .getAppWidgetIds(ComponentName(application, CollectorWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
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
        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork("UploadToDrive", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest)
    }
}
