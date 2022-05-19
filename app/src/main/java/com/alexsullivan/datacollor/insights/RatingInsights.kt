package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexsullivan.datacollor.RatingView
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.utils.titlecase
import java.time.LocalDate

@Composable
fun RatingUi(state: InsightsViewModel.UiState.RatingUiState) {
    Column {
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
