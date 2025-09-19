package org.godotengine.plugin.android.notifications.utils.enums

enum class NotificationError(val value: Int) {
    Void(1),
    NoContext(2),
    NoSetup(3),
    InvalidNotificationData(4),
    JsonError(5),
    ImageNotFound(6),
    IDLimit(7),
    PermissionNeeded(8),
    InvalidId(9),
}