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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@DelicateCoroutinesApi
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
            val trackables = getTrackingManager(context).getEnabledTrackables()
            val trackableEntities = getTrackingManager(context).getTodaysTrackableEntities()
            for (trackable in trackables) {
                val associatedTrackableEntity =
                    trackableEntities.firstOrNull { it.trackableId == trackable.id }
                val executed = associatedTrackableEntity?.executed ?: false
                val checkbox =
                    if (executed) R.drawable.ic_baseline_check_box_24 else R.drawable.ic_baseline_check_box_outline_blank_24
                val item = RemoteViews(context.packageName, R.layout.trackable_item)
                remoteViews.addView(R.id.grid, item)
                item.setTextViewText(R.id.text, trackable.title)
                item.setImageViewResource(R.id.check, checkbox)
                item.setOnClickPendingIntent(
                    R.id.check,
                    getPendingSelfIntent(context, trackable.title)
                )
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun getTrackingManager(context: Context): TrackableManager {
        if (trackableManager == null) {
            trackableManager = TrackableManager(TrackableEntityDatabase.getDatabase(context))
        }
        return trackableManager!!
    }
}

