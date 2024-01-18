package com.alexsullivan.datacollor.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun RatingMonth(ratings: List<Pair<LocalDate, Rating>>) {
    val colorDates = ratings.map { it.first to colorResource(id = it.second.colorRes) }
    Canvas(modifier = Modifier.fillMaxSize()) {
        // We want our days to be square, so choose the longest axis - either width or height
        val square = size.width.coerceAtMost(size.height)
        // Then we're limited by the number of days in a week, 7, over number of weeks, 5.
        val cellSize = square / 7
        colorDates.forEach { (date, color) ->
            val numberOfWeeks = date.numberOfWeeks()
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
    RatingMonth(entries)
}
