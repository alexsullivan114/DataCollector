package com.alexsullivan.datacollor.database

import androidx.room.Entity

@Entity(tableName = "enabled_trackable_table", primaryKeys = ["id"])
data class EnabledTrackable(val id: Int)
