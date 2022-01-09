package com.alexsullivan.datacollor.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.alexsullivan.datacollor.CollectorWidget

fun refreshWidget(context: Context) {
    val intent = Intent(context, CollectorWidget::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    val ids: IntArray = AppWidgetManager.getInstance(context.applicationContext)
        .getAppWidgetIds(ComponentName(context.applicationContext, CollectorWidget::class.java))
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    context.sendBroadcast(intent)
}
