package com.alexsullivan.datacollor.utils

import com.alexsullivan.datacollor.R
import com.alexsullivan.datacollor.database.entities.Rating

val Rating.colorRes
    get() = when (this) {
        Rating.TERRIBLE -> R.color.ratingTerrible
        Rating.POOR -> R.color.ratingPoor
        Rating.MEDIOCRE -> R.color.ratingMediocre
        Rating.GOOD -> R.color.ratingGood
        Rating.GREAT -> R.color.ratingGreat
    }
