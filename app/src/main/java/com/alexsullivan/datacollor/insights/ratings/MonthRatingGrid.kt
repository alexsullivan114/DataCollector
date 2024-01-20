package com.alexsullivan.datacollor.insights.ratings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.alexsullivan.datacollor.database.entities.Rating
import com.alexsullivan.datacollor.utils.colorRes
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale


@Composable
fun MonthRatingGrid(days: List<MonthRatingGridDay>) {
    val colorDates = days.map {
        val color = if (it.rating != null) {
            colorResource(it.rating.colorRes)
        } else {
            Color.Gray
        }
        it.date to color
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        // We want our days to be square, so choose the longest axis - either width or height
        val square = size.width.coerceAtMost(size.height)
        // Then we're limited by the number of days in a week, 7, over number of weeks, 5.
        val cellSize = square / 7
        val numberOfWeeks = days[0].date.numberOfWeeks()
        colorDates.forEach { (date, color) ->
            // Get the week of month
            val weekFields = WeekFields.of(Locale.getDefault())
            val weekOfMonth = date[weekFields.weekOfMonth()] - 1
            // Since we're not using the whole space we want to center our drawing, so add
            // a vertical offset
            val verticalCenteringOffset = (size.height - cellSize * numberOfWeeks) / 2
            val verticalOffset = (weekOfMonth * cellSize) + verticalCenteringOffset
            // And do the same for a horizontal offset.
            val horizontalCenteringOffset = (size.width - cellSize * 7) / 2
            val horizontalOffset = (date.dayOfWeek.value % 7) * cellSize + horizontalCenteringOffset
            val offset = Offset(horizontalOffset, verticalOffset)
            drawRect(
                brush = SolidColor(color),
                // The +1 is weird, but without it you see a faint white line between each cell.
                size = Size(cellSize + 1, cellSize + 1),
                topLeft = offset
            )
        }
    }
}

private fun LocalDate.numberOfWeeks(): Int {
    val firstDayOfMonth: LocalDate = LocalDate.of(year, month, 1)
    val lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth())

    val weekFields = WeekFields.of(Locale.getDefault())
    val weekNumberStart = firstDayOfMonth[weekFields.weekOfWeekBasedYear()]
    var weekNumberEnd = lastDayOfMonth[weekFields.weekOfWeekBasedYear()]

    // Adjust for the end of the year. We can run into scenarios where the last week in the year
    // is represented as the start of the new year instead of the last week of the year.
    if (weekNumberEnd == 1) {
        val lastDayOfPreviousYear =
            lastDayOfMonth.minusYears(1).with(TemporalAdjusters.lastDayOfYear())
        weekNumberEnd = lastDayOfPreviousYear[weekFields.weekOfWeekBasedYear()]
    }

    return weekNumberEnd - weekNumberStart + 1
}

@Preview
@Composable
fun RatingMonthPreview() {
    val entries = listOf(
        MonthRatingGridDay(LocalDate.of(2023, 12, 1),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 2),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 3),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 4),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 5),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 6),  null),
        MonthRatingGridDay(LocalDate.of(2023, 12, 7),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 8),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 9),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 10),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 11),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 12),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 13),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 14),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 15),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 16),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 17),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 18),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 19),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 20),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 21),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 22),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 23),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 24),  Rating.MEDIOCRE),
        MonthRatingGridDay(LocalDate.of(2023, 12, 25),  Rating.MEDIOCRE),
        MonthRatingGridDay(LocalDate.of(2023, 12, 26),  Rating.POOR),
        MonthRatingGridDay(LocalDate.of(2023, 12, 27),  Rating.POOR),
        MonthRatingGridDay(LocalDate.of(2023, 12, 28),  Rating.GREAT),
        MonthRatingGridDay(LocalDate.of(2023, 12, 29),  Rating.GOOD),
        MonthRatingGridDay(LocalDate.of(2023, 12, 30),  Rating.GOOD),
        MonthRatingGridDay(LocalDate.of(2023, 12, 31),  Rating.TERRIBLE),
    )
    MonthRatingGrid(entries)
}

fun fillMissingDays() {
//    val colorDates = ratings.map { it.first to colorResource(id = it.second.colorRes) }.sortedBy { it.first }.toMutableList()
//    for (i in 0 until colorDates.size) {
//        if (i != 0 && colorDates[i - 1].first != colorDates[i].first.minusDays(1)) {
//            colorDates.add(i, colorDates[i].first.minusDays(1) to Color.Gray)
//        }
//    }
}
