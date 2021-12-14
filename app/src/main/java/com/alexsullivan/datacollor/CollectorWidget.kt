package com.alexsullivan.datacollor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.alexsullivan.datacollor.database.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
            val action = intent.action ?: return@launch
            val enabledTrackables = getTrackingManager(context).getEnabledTrackables()
            Log.d("CollectorWidget", "Action: $action")
            if (enabledTrackables.map { it.title}.any { action.contains(it) }) {
                val trackable = enabledTrackables.first { action.contains(it.title) }
                Log.d("CollectorWidget", "Trackable ${trackable}")
                val trackingManager = getTrackingManager(context)
                GlobalScope.launch {
                    when (trackable.type) {
                        TrackableType.BOOLEAN -> trackingManager.toggle(trackable)
                        TrackableType.NUMBER -> {
                            if (action.contains("increment")) {
                                trackingManager.updateCount(trackable, true)
                            } else {
                                trackingManager.updateCount(trackable, false)
                            }
                        }
                    }
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
        // TODO: Pending intent mutability flag?
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
            val trackables = getTrackingManager(context).getEnabledTrackables().sortedBy { it.title }
            val trackableEntities = getTrackingManager(context).getTodaysTrackableEntities()
            for (trackable in trackables) {
                val entity = trackableEntities.firstOrNull { it.trackableId == trackable.id }!!
                val item = when (entity) {
                    is TrackableEntity.Boolean -> createBooleanTrackableView(context, trackable, entity.booleanEntity)
                    is TrackableEntity.Number -> createNumberTrackableView(context, trackable, entity.numberEntity)
                }
                remoteViews.addView(R.id.grid, item)
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun createNumberTrackableView(
        context: Context,
        trackable: Trackable,
        entity: NumberTrackableEntity
    ): RemoteViews {
        val item = RemoteViews(context.packageName, R.layout.number_trackable_item)
        item.setTextViewText(R.id.text, trackable.title)
        item.setTextViewText(R.id.count, entity.count.toString())
        item.setOnClickPendingIntent(
            R.id.increment,
            getPendingSelfIntent(context, trackable.title + "-increment")
        )
        item.setOnClickPendingIntent(
            R.id.decrement,
            getPendingSelfIntent(context, trackable.title + "-decrement")
        )
        return item
    }

    private fun createBooleanTrackableView(
        context: Context,
        trackable: Trackable,
        entity: BooleanTrackableEntity
    ): RemoteViews {
        val checkbox =
            if (entity.executed) R.drawable.ic_baseline_check_box_24 else R.drawable.ic_baseline_check_box_outline_blank_24
        val item = RemoteViews(context.packageName, R.layout.boolean_trackable_item)
        item.setTextViewText(R.id.text, trackable.title)
        item.setImageViewResource(R.id.check, checkbox)
        item.setOnClickPendingIntent(
            R.id.check,
            getPendingSelfIntent(context, trackable.title)
        )
        return item
    }

    private fun getTrackingManager(context: Context): TrackableManager {
        if (trackableManager == null) {
            trackableManager = TrackableManager(TrackableEntityDatabase.getDatabase(context))
        }
        return trackableManager!!
    }
}

