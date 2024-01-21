package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexsullivan.datacollor.utils.displayableString
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TimeUi(state: InsightsViewModel.UiState.TimeUiState) {
    Column {
        Calendar(toggledTimes = state.toggledTimes)
        Text(
            text = buildAnnotatedString {
                append("You toggle ${state.trackableTitle} at")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(" ${state.averageToggledTime.displayableString()} ")
                }
                append("on average.")
            },
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun Calendar(modifier: Modifier = Modifier, toggledTimes: List<Pair<LocalDate, LocalTime>>) {
   InsightsCalendar(modifier = modifier) { calendarDate ->
       val toggledTime = toggledTimes.firstOrNull { it.first == calendarDate }?.second
       if (toggledTime != null) {
           Text(toggledTime.displayableString())
       }
   }
}
