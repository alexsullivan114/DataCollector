package com.alexsullivan.datacollor

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun AppTheme(content: @Composable () -> Unit) {
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = statusBarColor
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
//        }
//    }
    MaterialTheme(
        colorScheme = dynamicLightColorScheme(LocalContext.current)
    ) {
        val statusBarColor = MaterialTheme.colorScheme.background.toArgb()
        val view = LocalView.current
        val activity = LocalContext.current as Activity
        content()
        SideEffect {
            val window = activity.window
            window.statusBarColor = statusBarColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }
}
