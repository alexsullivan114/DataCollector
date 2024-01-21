package com.alexsullivan.datacollor.insights.ratings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexsullivan.datacollor.RatingView
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.insights.InsightsCalendar
import com.alexsullivan.datacollor.insights.InsightsViewModel
import com.alexsullivan.datacollor.utils.titlecase
import java.time.LocalDate

@Composable
fun RatingUi(state: InsightsViewModel.UiState.RatingUiState) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Calendar(state.dateRatings)
        Text(
            text = buildAnnotatedString {
                append("The day with the highest rating for ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(state.trackableTitle)
                }
                append(" is ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append("${state.highestRatedDay.toString().titlecase()}.")
                }
            },
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = buildAnnotatedString {
                append("The day with the lowest rating for ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(state.trackableTitle)
                }
                append(" is ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append("${state.lowestRatedDay.toString().titlecase()}.")
                }
            },
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = buildAnnotatedString {
                append("The average rating for ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(state.trackableTitle)
                }
                append(" is between ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(state.averageRatingBound.first.toString().titlecase())
                }
                append(" and ")
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append("${state.averageRatingBound.second.toString().titlecase()}.")
                }
            },
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        state.monthRatings.forEach {
            Text(text = it.title, style = MaterialTheme.typography.labelLarge)
            Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                MonthRatingGrid(days = it.days)
            }
        }
    }
}

@Composable
private fun Calendar(dateRatings: List<Pair<LocalDate, Rating>>, modifier: Modifier = Modifier) {
    InsightsCalendar(modifier = modifier) { date ->
        val rating = dateRatings.firstOrNull { it.first == date }?.second
        if (rating != null) {
            RatingView(rating = rating, modifier = Modifier.size(12.dp))
        }
    }
}
