package com.alexsullivan.datacollor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

private val states = mutableListOf(false)
/**
 * Implementation of App Widget functionality.
 */
class CollectorWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (tag == intent.action) {
            Log.d("Woof", "Old States: $states")
            states[0] = !states[0]
            Log.d("Woof", "new States: $states")
            val ids: IntArray = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, CollectorWidget::class.java))
            onUpdate(context, AppWidgetManager.getInstance(context), ids)
        }
    }

    private fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.collector_widget)
        remoteViews.setOnClickPendingIntent(R.id.check1, getPendingSelfIntent(context, tag))
        val checkbox = if (states[0]) R.drawable.ic_baseline_check_box_24 else R.drawable.ic_baseline_check_box_outline_blank_24
        remoteViews.setImageViewResource(R.id.check1,  checkbox)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    companion object {
        private const val tag = "Tag"
    }
}

