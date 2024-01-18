package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexsullivan.datacollor.RatingView
import com.alexsullivan.datacollor.database.entities.Rating
import java.time.LocalDate

@Composable
fun RatingUi(state: InsightsViewModel.UiState.RatingUiState) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        Calendar(state.dateRatings)
//        Text(
//            text = buildAnnotatedString {
//                append("The day with the highest rating for ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append(state.trackableTitle)
//                }
//                append(" is ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append("${state.highestRatedDay.toString().titlecase()}.")
//                }
//            },
//            fontSize = 18.sp,
//            modifier = Modifier.padding(16.dp)
//        )
//        Text(
//            text = buildAnnotatedString {
//                append("The day with the lowest rating for ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append(state.trackableTitle)
//                }
//                append(" is ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append("${state.lowestRatedDay.toString().titlecase()}.")
//                }
//            },
//            fontSize = 18.sp,
//            modifier = Modifier.padding(16.dp)
//        )
//        Text(
//            text = buildAnnotatedString {
//                append("The average rating for ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append(state.trackableTitle)
//                }
//                append(" is between ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append(state.averageRatingBound.first.toString().titlecase())
//                }
//                append(" and ")
//                withStyle(style = SpanStyle(color = Color.Blue)) {
//                    append("${state.averageRatingBound.second.toString().titlecase()}.")
//                }
//            },
//            fontSize = 18.sp,
//            modifier = Modifier.padding(16.dp)
//        )
        val entries = listOf(
            LocalDate.of(2023, 12, 1) to Rating.GREAT,
            LocalDate.of(2023, 12, 2) to Rating.GREAT,
            LocalDate.of(2023, 12, 3) to Rating.GREAT,
            LocalDate.of(2023, 12, 4) to Rating.GREAT,
            LocalDate.of(2023, 12, 5) to Rating.GREAT,
            LocalDate.of(2023, 12, 6) to Rating.GREAT,
            LocalDate.of(2023, 12, 7) to Rating.GREAT,
            LocalDate.of(2023, 12, 8) to Rating.GREAT,
            LocalDate.of(2023, 12, 9) to Rating.GREAT,
            LocalDate.of(2023, 12, 10) to Rating.GREAT,
            LocalDate.of(2023, 12, 11) to Rating.GREAT,
            LocalDate.of(2023, 12, 12) to Rating.GREAT,
            LocalDate.of(2023, 12, 13) to Rating.GREAT,
            LocalDate.of(2023, 12, 14) to Rating.GREAT,
            LocalDate.of(2023, 12, 15) to Rating.GREAT,
            LocalDate.of(2023, 12, 16) to Rating.GREAT,
            LocalDate.of(2023, 12, 17) to Rating.GREAT,
            LocalDate.of(2023, 12, 18) to Rating.GREAT,
            LocalDate.of(2023, 12, 19) to Rating.GREAT,
            LocalDate.of(2023, 12, 20) to Rating.GREAT,
            LocalDate.of(2023, 12, 21) to Rating.GREAT,
            LocalDate.of(2023, 12, 22) to Rating.GREAT,
            LocalDate.of(2023, 12, 23) to Rating.GREAT,
            LocalDate.of(2023, 12, 24) to Rating.MEDIOCRE,
            LocalDate.of(2023, 12, 25) to Rating.MEDIOCRE,
            LocalDate.of(2023, 12, 26) to Rating.POOR,
            LocalDate.of(2023, 12, 27) to Rating.POOR,
            LocalDate.of(2023, 12, 28) to Rating.GREAT,
            LocalDate.of(2023, 12, 29) to Rating.GOOD,
            LocalDate.of(2023, 12, 30) to Rating.GOOD,
            LocalDate.of(2023, 12, 31) to Rating.TERRIBLE,
        )

        Box(modifier = Modifier.height(350.dp).border(5.dp, Color.Cyan)) {
            RatingMonth(ratings = entries)
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
