package org.godotengine.plugin.android.notifications.utils

import androidx.core.app.NotificationCompat

enum class Priority(val value: Int) {
    MIN(NotificationCompat.PRIORITY_MIN),
    LOW(NotificationCompat.PRIORITY_LOW),
    DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
    HIGH(NotificationCompat.PRIORITY_HIGH),
    MAX(NotificationCompat.PRIORITY_MAX)
}
