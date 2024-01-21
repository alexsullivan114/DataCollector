package com.alexsullivan.datacollor.insights

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.boguszpawlowski.composecalendar.StaticCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.selection.EmptySelectionState
import java.time.LocalDate

@Composable
fun InsightsCalendar(modifier: Modifier, indicator: @Composable (date: LocalDate) -> Unit) {
    StaticCalendar(
        modifier = modifier.animateContentSize(),
        dayContent = { dayState ->
            DayContent(dayState, indicator)
        }
    )
}

@Composable
private fun DayContent(
    dayState: DayState<EmptySelectionState>,
    content: @Composable (date: LocalDate) -> Unit
) {
    val isToday = dayState.date == LocalDate.now()
    val afterToday = dayState.date > LocalDate.now()
    val textColor = if (afterToday) Color.Gray else Color.Black
    val fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.sizeIn(minHeight = 50.dp, minWidth = 50.dp)
    ) {
        Text(
            text = dayState.date.dayOfMonth.toString(),
            modifier = Modifier.padding(6.dp),
            style = TextStyle(color = textColor, fontWeight = fontWeight)
        )
        content(dayState.date)
    }
}
