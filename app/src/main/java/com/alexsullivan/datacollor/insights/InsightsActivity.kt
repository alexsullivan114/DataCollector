package com.alexsullivan.datacollor.insights

import android.content.Context
import android.content.Intent
import android.graphics.fonts.FontStyle
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import com.alexsullivan.datacollor.previousdays.PreviousDaysActivity
import com.alexsullivan.datacollor.settings.SettingsActivity

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

    @Composable
    private fun BooleanUi(state: InsightsViewModel.UiState.BooleanUiState) {
        Column {
            Text(
                text = buildAnnotatedString {
                    append("You've toggled ${state.trackableTitle} a total of")
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append(" ${state.totalCount} ")
                    }
                    append("times.")
                },
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = buildAnnotatedString {
                    append("Since the start of the year, you've toggled ${state.trackableTitle}")
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append(" ${state.yearStartCount} ")
                    }
                    append("times.")
                },
                lineHeight = 30.sp,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
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
