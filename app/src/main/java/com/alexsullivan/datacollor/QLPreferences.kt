package com.alexsullivan.datacollor

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class QLPreferences @Inject constructor(@ApplicationContext context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("qualified_life", Context.MODE_PRIVATE)

    private val backupFileIdKey = "driveFileId"
    var backupFileId: String?
        get() = prefs.getString(backupFileIdKey, null)
        set(value) {
            prefs.edit().putString(backupFileIdKey, value).apply()
        }
}
