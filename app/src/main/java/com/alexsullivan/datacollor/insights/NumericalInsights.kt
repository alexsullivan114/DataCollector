package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun NumericUi(state: InsightsViewModel.UiState.NumericUiState) {
    Column {
        Calendar(datePairs = state.dateCounts)
    }
}

@Composable
private fun Calendar(datePairs: List<Pair<LocalDate, Int>>, modifier: Modifier = Modifier) {
    InsightsCalendar(modifier = modifier) { date ->
        val count = datePairs.firstOrNull { it.first == date }?.second
        if (count != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(28.dp)
                    .background(Color.Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(count.toString(), color = Color.White)
            }
        }
    }
}
