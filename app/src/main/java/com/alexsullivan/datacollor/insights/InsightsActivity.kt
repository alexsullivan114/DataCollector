package com.alexsullivan.datacollor.insights

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.insights.ratings.RatingUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class InsightsActivity : AppCompatActivity() {
    @Inject lateinit var insightsViewModelFactory: InsightsViewModelFactory

    private val viewModel: InsightsViewModel by viewModels {
        InsightsViewModel.provideFactory(insightsViewModelFactory, intent.getStringExtra(ID_KEY)!!)
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
                Box(modifier = Modifier.padding(it)) {
                    when (val state = uiState.value) {
                        is InsightsViewModel.UiState.BooleanUiState -> BooleanUi(state)
                        is InsightsViewModel.UiState.NumericUiState -> NumericUi(state)
                        is InsightsViewModel.UiState.RatingUiState -> RatingUi(state)
                        is InsightsViewModel.UiState.TimeUiState -> TimeUi(state)
                        null -> {}
                    }
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
