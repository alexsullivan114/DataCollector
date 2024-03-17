package com.alexsullivan.datacollor

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val backupFileIdKey = "driveFileId"
private const val openAiCsvFileIdKey = "openAiCsvFileId"
private const val openAiAssistantIdKey = "openAiAssistantId"
class QLPreferences @Inject constructor(@ApplicationContext context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("qualified_life", Context.MODE_PRIVATE)

    var backupFileId: String?
        get() = prefs.getString(backupFileIdKey, null)
        set(value) {
            prefs.edit().putString(backupFileIdKey, value).apply()
        }

    var openAiCsvFileId: String?
        get() = prefs.getString(openAiCsvFileIdKey, null)
        set(value) {
            prefs.edit().putString(openAiCsvFileIdKey, value).apply()
        }

    var openAiAssistantId: String?
        get() = prefs.getString(openAiAssistantIdKey, null)
        set(value) {
            prefs.edit().putString(openAiAssistantIdKey, value).apply()
        }
}
