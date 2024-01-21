package com.alexsullivan.datacollor.insights.ratings

import com.alexsullivan.datacollor.database.entities.Rating
import java.time.LocalDate

data class MonthRating(val title: String, val days: List<MonthRatingGridDay>)
data class MonthRatingGridDay(val date: LocalDate, val rating: Rating?)
