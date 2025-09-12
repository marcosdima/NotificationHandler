package org.godotengine.plugin.android.notifications

import android.Manifest
import android.util.Log
import org.json.JSONArray
import android.widget.Toast
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi

import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import org.godotengine.plugin.android.notifications.utils.Logger
import org.godotengine.plugin.android.notifications.utils.ResourceLoader


class NotificationHandler(godot: Godot): GodotPlugin(godot) {
    private var initialized: Boolean = false

    companion object {
        const val ERROR_RETURN: Int = -1
        const val REQUEST_CODE_NOTIFICATIONS: Int = 1000
        const val NOTIFICATION_ID: Int = 100
    }

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    @UsedByGodot
    fun echo(echoThis: String) {
        runOnUiThread {
            Toast.makeText(activity, echoThis, Toast.LENGTH_LONG).show()
            Log.v(pluginName, "Echo!")
        }
    }

    @UsedByGodot
    fun setup(
        channelsJson: String,
    ) {
        // If activity is null, do nothing
        val context = activity ?: return

        // Set initialized flag.
        this.initialized = true

        // Create the channels provided.
        try {
            val arr = JSONArray(channelsJson)

            val channelHandler = ChannelHandler(context)
            for (i in 0 until arr.length()) {
                val ch = arr.getJSONObject(i)
                channelHandler.createChannelFromJson(ch)
            }

            Log.d(BuildConfig.GODOT_PLUGIN_NAME, "Setup completed!")
        } catch (e: Exception) {
            Log.e(BuildConfig.GODOT_PLUGIN_NAME, "Invalid JSON: $channelsJson", e)
        }
    }

    @UsedByGodot
    fun triggerNotification(
        channelId: String,
        title: String,
        content: String,
        smallIcon: String,
    ): Int {
        val context = activity ?: return ERROR_RETURN

        if (!initialized) {
            Logger.error("You must call setup first!")
            return ERROR_RETURN
        }

        Log.d(
            BuildConfig.GODOT_PLUGIN_NAME,
            listOf(
                "Channel ID: $channelId",
                "Title: $title",
                "Content: $content",
                "Icon: $smallIcon"
            ).joinToString(separator = " | ")
        )

        val loader = ResourceLoader(context)
        val smallIconBitmap: Bitmap = loader.loadImage(smallIcon) ?: return ERROR_RETURN
        val smallIcon: IconCompat = IconCompat.createWithBitmap(smallIconBitmap)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        return with(NotificationManagerCompat.from(context)) {
            // Just for En Android 13+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionStatus = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
                if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                    Logger.error("POST_NOTIFICATIONS permission not granted!")
                    return@with ERROR_RETURN
                }
            }

            val notificationId = NOTIFICATION_ID
            notify(notificationId, builder.build())
            Logger.debug("Notification sent with ID $notificationId")

            // Returns notificationId to godot.
            notificationId
        }
    }
}
