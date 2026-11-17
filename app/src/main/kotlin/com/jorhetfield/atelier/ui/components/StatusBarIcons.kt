package com.jorhetfield.atelier.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// While a picture sits under a system bar, its icons must be light whatever the theme says.
@Composable
fun LightSystemBarIcons(
    statusBars: Boolean,
    navigationBars: Boolean = false,
) {
    if (LocalInspectionMode.current) return
    val view = LocalView.current
    val window = view.context.findActivity()?.window ?: return

    DisposableEffect(statusBars, navigationBars) {
        val controller = WindowCompat.getInsetsController(window, view)
        val previousStatus = controller.isAppearanceLightStatusBars
        val previousNavigation = controller.isAppearanceLightNavigationBars
        if (statusBars) controller.isAppearanceLightStatusBars = false
        if (navigationBars) controller.isAppearanceLightNavigationBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = previousStatus
            controller.isAppearanceLightNavigationBars = previousNavigation
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
