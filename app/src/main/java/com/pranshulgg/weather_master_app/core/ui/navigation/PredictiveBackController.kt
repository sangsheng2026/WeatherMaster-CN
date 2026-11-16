package com.pranshulgg.weather_master_app.core.ui.navigation

import android.os.Build
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher

/**
 * Predictive back (the swipe-from-edge preview that scales the page down) can be
 * detached at runtime.
 *
 * [OnBackPressedDispatcher] is normally bound to the window's
 * [OnBackInvokedDispatcher] in `ComponentActivity.onCreate`. When that binding is
 * removed the platform no longer receives back animation callbacks, so the gesture
 * falls back to the classic "press back" path: no live preview, no predictive
 * animation, just the usual navigation transition.
 *
 * Binding it back re-enables the preview without restarting the app.
 */
object PredictiveBackController {

    private var windowDispatcher: OnBackInvokedDispatcher? = null

    fun attach(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        windowDispatcher = activity.onBackInvokedDispatcher
    }

    fun setEnabled(activity: ComponentActivity, enabled: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        apply(activity, if (enabled) windowDispatcher else null)
    }

    private fun apply(activity: ComponentActivity, dispatcher: OnBackInvokedDispatcher?) {
        // The setter is public on the platform class but annotated @RestrictTo in
        // AndroidX, so reach it reflectively and stay a no-op if it ever disappears.
        runCatching {
            OnBackPressedDispatcher::class.java
                .getMethod("setOnBackInvokedDispatcher", OnBackInvokedDispatcher::class.java)
                .invoke(activity.onBackPressedDispatcher, dispatcher)
        }
    }
}