package com.alexsullivan.datacollor.insights

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexsullivan.datacollor.insights.InsightsViewModel.UiState.BooleanUiState
import io.github.boguszpawlowski.composecalendar.StaticCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.selection.EmptySelectionState
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
fun Calendar(dates: List<LocalDate>, modifier: Modifier = Modifier) {
    StaticCalendar(
        modifier = modifier.animateContentSize(),
        dayContent = { dayState ->
            val toggled = dates.contains(dayState.date)
            DayContent(dayState = dayState, toggled = toggled)
        }
    )
}

@Composable
fun DayContent(dayState: DayState<EmptySelectionState>, toggled: Boolean) {
    val isToday = dayState.date == LocalDate.now()
    val afterToday = dayState.date > LocalDate.now()
    val textColor = if (afterToday) Color.Gray else Color.Black
    val fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.size(50.dp)) {
        Text(
            text = dayState.date.dayOfMonth.toString(),
            modifier = Modifier.padding(6.dp),
            style = TextStyle(color = textColor, fontWeight = fontWeight)
        )
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
