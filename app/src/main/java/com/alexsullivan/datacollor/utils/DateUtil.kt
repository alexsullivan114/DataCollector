package com.alexsullivan.datacollor.utils

import java.time.LocalTime
import java.time.OffsetDateTime

fun midnight(): OffsetDateTime {
    return OffsetDateTime.now().with(LocalTime.MIDNIGHT)
}

val OffsetDateTime.midnight: OffsetDateTime
    get() {
        return with(LocalTime.MIDNIGHT)
    }
