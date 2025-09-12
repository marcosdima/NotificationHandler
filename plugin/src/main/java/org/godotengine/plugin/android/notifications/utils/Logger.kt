package org.godotengine.plugin.android.notifications.utils

import android.util.Log
import org.godotengine.plugin.android.notifications.BuildConfig

object Logger {
    fun debug(content: String) {
        Log.d(BuildConfig.GODOT_PLUGIN_NAME, content)
    }

    fun error(content: String) {
        Log.e(BuildConfig.GODOT_PLUGIN_NAME, content)
    }
}