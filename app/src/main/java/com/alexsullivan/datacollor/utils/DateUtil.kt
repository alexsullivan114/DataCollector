package com.alexsullivan.datacollor.utils

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun midnight(): Date {
    val calendar: Calendar = GregorianCalendar()
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    return calendar.time
}
