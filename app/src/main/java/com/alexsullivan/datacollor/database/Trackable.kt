package com.alexsullivan.datacollor.database

import androidx.room.Entity

@Entity(tableName = "trackable_table", primaryKeys = ["id"])
data class Trackable(val id: Int, val title: String, val enabled: Boolean)
