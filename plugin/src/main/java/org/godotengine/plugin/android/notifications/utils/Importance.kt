package org.godotengine.plugin.android.notifications.utils

import android.app.NotificationManager

enum class Importance(val value: Int) {
    NONE(NotificationManager.IMPORTANCE_NONE),
    LOW(NotificationManager.IMPORTANCE_LOW),
    DEFAULT(NotificationManager.IMPORTANCE_DEFAULT),
    HIGH(NotificationManager.IMPORTANCE_HIGH)
}