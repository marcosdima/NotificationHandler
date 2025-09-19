package org.godotengine.plugin.android.notifications.utils.enums

import android.app.NotificationManager

enum class Importance(val value: Int) {
    NONE(NotificationManager.IMPORTANCE_NONE),
    MIN(NotificationManager.IMPORTANCE_MIN),
    LOW(NotificationManager.IMPORTANCE_LOW),
    DEFAULT(NotificationManager.IMPORTANCE_DEFAULT),
    HIGH(NotificationManager.IMPORTANCE_HIGH)
}