package com.alexsullivan.datacollor.utils

import java.util.*

fun String.titlecase(): String {
    return lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }
}
