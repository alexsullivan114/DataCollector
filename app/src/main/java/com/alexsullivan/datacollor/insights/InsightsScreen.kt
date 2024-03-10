@file:OptIn(ExperimentalMaterial3Api::class)

package com.alexsullivan.datacollor.insights

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.alexsullivan.datacollor.AppTheme
import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.insights.ratings.RatingUi

@Composable
fun InsightsScreen(onNavigateBack: () -> Unit) {
    val viewModel = hiltViewModel<InsightsViewModel>()
    AppTheme {
        Scaffold(
            topBar = { QLAppBar(onNavigateBack) },
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
fun QLAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.insights)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack,"Navigate back")
            }
        }
    )
}
