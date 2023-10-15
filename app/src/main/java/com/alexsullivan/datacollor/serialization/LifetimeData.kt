package com.alexsullivan.datacollor.serialization

import com.alexsullivan.datacollor.database.Trackable

data class LifetimeData(val trackables: List<Trackable>, val days: List<DayData>)
