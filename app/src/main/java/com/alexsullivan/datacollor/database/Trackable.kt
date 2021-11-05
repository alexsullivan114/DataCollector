package com.alexsullivan.datacollor.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// The index is primarily to make sure that we don't have duplicate titles.
@Entity(tableName = "trackable_table", indices = [Index(value = ["title"], unique = true)])
data class Trackable(@PrimaryKey val id: String, val title: String, val enabled: Boolean)
