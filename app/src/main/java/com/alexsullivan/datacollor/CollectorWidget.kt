package com.alexsullivan.datacollor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.alexsullivan.datacollor.database.Trackable
import com.alexsullivan.datacollor.database.TrackableEntityDatabase
import com.alexsullivan.datacollor.database.TrackableManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CollectorWidget : AppWidgetProvider() {

    private var trackableManager: TrackableManager? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        GlobalScope.launch {
            getTrackingManager(context).update()
            // There may be multiple widgets active, so update all of them
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
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
        GlobalScope.launch {
            val enabledTrackables = getTrackingManager(context).getEnabledTrackables()
            if (enabledTrackables.map { it.title}.contains(intent.action)) {
                val trackable = enabledTrackables.first { it.title == intent.action }
                GlobalScope.launch {
                    getTrackingManager(context).toggle(trackable)
                    val ids: IntArray = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, CollectorWidget::class.java))
                    onUpdate(context, AppWidgetManager.getInstance(context), ids)
                }
            }
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
        GlobalScope.launch {
            val remoteViews = RemoteViews(context.packageName, R.layout.collector_widget)
            remoteViews.removeAllViews(R.id.grid)
            for (entry in getTrackingManager(context).state) {
                val checkbox = if (entry.value) R.drawable.ic_baseline_check_box_24 else R.drawable.ic_baseline_check_box_outline_blank_24
                val item = RemoteViews(context.packageName, R.layout.trackable_item)
                remoteViews.addView(R.id.grid, item)
                item.setTextViewText(R.id.text, entry.key.title)
                item.setImageViewResource(R.id.check, checkbox)
                item.setOnClickPendingIntent(R.id.check, getPendingSelfIntent(context, entry.key.title))
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private suspend fun getTrackingManager(context: Context): TrackableManager {
        if (trackableManager == null) {
            trackableManager = TrackableManager(TrackableEntityDatabase.getDatabase(context))
            println("We be initing from collector widget: $this")
            // TODO: This gets called a lot, and I _think_ that's the only reason deleting trackables
            // is reflected in the actual widget. Maybe that's ok but not sure.
            trackableManager!!.init()
        }
        return trackableManager!!
    }
}

