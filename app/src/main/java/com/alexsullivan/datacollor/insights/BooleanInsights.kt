package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.BooleanUiState
import java.time.LocalDate

@Composable
fun BooleanUi(state: BooleanUiState) {
    Column {
        Calendar(state.daysToggled, Modifier.padding(16.dp))
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
private fun Calendar(dates: List<LocalDate>, modifier: Modifier = Modifier) {
    InsightsCalendar(modifier = modifier) { date ->
        val toggled = dates.contains(date)
        if (toggled) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(8.dp)
                    .background(Color.Blue)
            )
        }
    }
}
