package com.alexsullivan.datacollor.application

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alexsullivan.datacollor.drive.DriveUploadWorker
import com.alexsullivan.datacollor.routing.Router
import com.alexsullivan.datacollor.utils.refreshWidget
import com.alexsullivan.datacollor.weather.WeatherWorker
import com.alexsullivan.datacollor.weather.location_import.TakeoutDataManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
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
            Router()
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
