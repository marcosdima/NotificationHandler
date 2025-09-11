package org.godotengine.plugin.android.notifications

import org.godotengine.plugin.android.notifications.utils.Importance

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import org.json.JSONObject

class ChannelHandler(val context: Context) {
    companion object {
        const val CHANNEL_ID = "channelId"
        const val CHANNEL_NAME = "name"
        const val CHANNEL_DESCRIPTION = "description"
        const val CHANNEL_IMPORTANCE = "importance"
    }

    fun createChannel(
        channelId: String,
        name: String,
        description: String,
        importance: Importance = Importance.DEFAULT,
    ) {
        val mChannel = NotificationChannel(channelId, name, importance.value)
        mChannel.description = description
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    fun createChannelFromJson(channelJson: JSONObject) {
        val channelId = channelJson.optString(CHANNEL_ID, "")
        val name = channelJson.optString(CHANNEL_NAME, "")
        val description = channelJson.optString(CHANNEL_DESCRIPTION, "This is a Channel")

        val importanceValue = channelJson.optInt(CHANNEL_IMPORTANCE, NotificationManager.IMPORTANCE_DEFAULT)
        val importance = Importance.values().firstOrNull { it.value == importanceValue } ?: Importance.DEFAULT

        if (channelId.isBlank() || name.isBlank()) {
            Log.e("NotificationHandler", "Missing fields: " +
                    listOfNotNull(
                        CHANNEL_ID.takeIf { channelId.isBlank() },
                        CHANNEL_NAME.takeIf { name.isBlank() }
                    ).joinToString(", ")
            )
            return
        }

        this.createChannel(channelId, name, description, importance)
    }
}