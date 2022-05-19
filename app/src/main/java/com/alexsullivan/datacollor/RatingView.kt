package com.alexsullivan.datacollor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.alexsullivan.datacollor.database.entities.Rating

@Composable
fun RatingView(rating: Rating, modifier: Modifier) {
    val colorRes = when (rating) {
        Rating.TERRIBLE -> R.color.ratingTerrible
        Rating.POOR -> R.color.ratingPoor
        Rating.MEDIOCRE -> R.color.ratingMediocre
        Rating.GOOD -> R.color.ratingGood
        Rating.GREAT -> R.color.ratingGreat
    }
    val cornerRadius = dimensionResource(id = R.dimen.ratingCorners)
    Box(
        modifier = modifier
            .background(colorResource(id = colorRes), shape = RoundedCornerShape(cornerRadius))
            .border(1.dp, Color.Black, shape = RoundedCornerShape(cornerRadius))
    )
}
