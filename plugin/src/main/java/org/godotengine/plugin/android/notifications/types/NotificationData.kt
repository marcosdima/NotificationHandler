package org.godotengine.plugin.android.notifications.types

import org.godotengine.plugin.android.notifications.utils.enums.Priority

data class NotificationData(
    val title: String,
    val content: String,
    val smallIcon: String,
    val priority: Priority = Priority.DEFAULT,
    val image: String = "",
    val bigText: String = "",
    val lines: List<String> = emptyList()
)
