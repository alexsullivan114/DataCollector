package com.alexsullivan.datacollor.database

import androidx.room.Entity

//enum class Trackable(val id: Int, val title: String) {
//    COFFEE_PAST_12(1, "Coffee past 12"),
//    ALCOHOL(2, "Alcohol"),
//    WEED(3, "Weed"),
//    SLEPT_WELL(4, "Slept well"),
//    WOKE_UP_RESTED(5, "Woke up rested"),
//    EXERCISE(6, "Exercise"),
//    MEDITATE(7, "Meditate"),
//    MORNING_TEETH(8, "Morning brush"),
//    EVENING_TEETH(9, "Evening brush")
//}
//
@Entity(tableName = "trackable_table", primaryKeys = ["id"])
data class Trackable(val id: Int, val title: String)
