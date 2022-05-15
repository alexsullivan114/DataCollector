package com.alexsullivan.datacollor.insights

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager

class InsightsActivity : AppCompatActivity() {
    private val viewModel: InsightsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val id = intent.getStringExtra(ID_KEY)!!
                val database = TrackableEntityDatabase.getDatabase(this@InsightsActivity)
                val trackableManager = TrackableManager(database)
                return InsightsViewModel(id, trackableManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsightsScreen()
        }
    }

    @Composable
    fun InsightsScreen() {
        AppTheme {
            Scaffold(
                topBar = { QLAppBar() },
            ) {
                val uiState = viewModel.uiFlow.collectAsState()
                when (val state = uiState.value) {
                    is InsightsViewModel.UiState.BooleanUiState -> BooleanUi(state)
                    null -> {}
                }
            }

        }
    }

    @Composable
    fun QLAppBar() {
        TopAppBar(
            title = { Text(stringResource(R.string.insights)) },
            navigationIcon = {
                IconButton(onClick = { finish() }) {
                    Icon(Icons.Filled.ArrowBack,"Navigate back")
                }
            }
        )
    }

    companion object {
        const val ID_KEY = "id"
        fun getIntent(trackableId: String, context: Context): Intent {
            val intent = Intent(context, InsightsActivity::class.java)
            intent.putExtra(ID_KEY, trackableId)
            return intent
        }
    }
}
