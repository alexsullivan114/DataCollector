package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun NumericUi(state: InsightsViewModel.UiState.NumericUiState) {
    Column {
        Calendar(datePairs = state.dateCounts)
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
        Text(
            text = buildAnnotatedString {
                append("On average, you toggle ${state.trackableTitle}")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(" %.2f ".format(state.perWeekCount))
                }
                append("times per week.")
            },
            lineHeight = 30.sp,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
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
