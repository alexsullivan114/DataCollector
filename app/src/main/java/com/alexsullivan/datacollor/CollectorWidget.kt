package com.alexsullivan.datacollor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private val states = mutableMapOf<Trackable, Boolean>().apply {
    for (trackable in Trackable.values()) {
        put(trackable, false)
    }
}

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
        if (Trackable.values().map { it.name }.contains(intent.action)) {
            val trackable = Trackable.values().first { it.name == intent.action }
            states[trackable] = !states.getValue(trackable)
            val ids: IntArray = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, CollectorWidget::class.java))
            onUpdate(context, AppWidgetManager.getInstance(context), ids)

            val calendar: Calendar = GregorianCalendar()
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            val date = calendar.time
            val trackableEntity = TrackableEntity(trackable, states.getValue(trackable), date)

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    TrackableEntityDatabase.getDatabase(context).trackableEntityDao()
                        .saveEntity(trackableEntity)
                    val entities = TrackableEntityDatabase.getDatabase(context).trackableEntityDao()
                        .getTrackableEntities()
                    Log.d("entities", "Entities: $entities")
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
        val remoteViews = RemoteViews(context.packageName, R.layout.collector_widget)
        remoteViews.removeAllViews(R.id.grid)
        for (trackable in Trackable.values()) {
            val checkbox =
                if (states.getValue(trackable)) R.drawable.ic_baseline_check_box_24 else R.drawable.ic_baseline_check_box_outline_blank_24
            val item = RemoteViews(context.packageName, R.layout.trackable_item)
            remoteViews.addView(R.id.grid, item)
            item.setTextViewText(R.id.text, trackable.title)
            item.setImageViewResource(R.id.check, checkbox)
            item.setOnClickPendingIntent(R.id.check, getPendingSelfIntent(context, trackable.name))
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}

