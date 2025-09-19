package org.godotengine.plugin.android.notifications.utils.enums

import androidx.core.app.NotificationCompat
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

enum class Priority(val value: Int) {
    MIN(NotificationCompat.PRIORITY_MIN),
    LOW(NotificationCompat.PRIORITY_LOW),
    DEFAULT(NotificationCompat.PRIORITY_DEFAULT),
    HIGH(NotificationCompat.PRIORITY_HIGH),
    MAX(NotificationCompat.PRIORITY_MAX);

    companion object {
        fun fromValue(value: Int): Priority =
            entries.find { it.value == value } ?: DEFAULT
    }
}

class PriorityAdapter {
    @FromJson
    fun fromJson(value: Int): Priority = Priority.fromValue(value)

    @ToJson
    fun toJson(priority: Priority): Int = priority.value
}