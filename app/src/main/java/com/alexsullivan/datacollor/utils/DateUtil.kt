package com.alexsullivan.datacollor.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun today(): LocalDate {
    return LocalDate.now()
}

fun LocalTime.displayableString(): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return this.format(formatter)
}
