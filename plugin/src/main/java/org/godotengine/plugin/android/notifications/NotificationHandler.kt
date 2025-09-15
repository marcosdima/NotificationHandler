package org.godotengine.plugin.android.notifications

import android.Manifest
import android.util.Log
import org.json.JSONArray
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import org.godotengine.plugin.android.notifications.types.Notification
import org.godotengine.plugin.android.notifications.types.NotificationData
import org.godotengine.plugin.android.notifications.utils.Logger

class NotificationHandler(godot: Godot): GodotPlugin(godot) {
    private var initialized: Boolean = false
    private val adapter = Notification.getMoshiBuilder().build().adapter(NotificationData::class.java)

    companion object {
        const val ERROR_RETURN: Int = -1 // TODO: Error enum.
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
    ): Int {
        // If activity is null, do nothing
        val context = activity ?: return ERROR_RETURN

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

            Logger.debug("Setup completed!")
        } catch (e: Exception) {
            Logger.error("Invalid JSON: $channelsJson \n Error: $e")
            return ERROR_RETURN
        }

        return 0
    }

    @UsedByGodot
    fun triggerNotification(
        channelId: String,
        notificationData: String
    ): Int {
        Logger.debug("Params: $channelId $notificationData")

        // If activity is null, do nothing.
        val context = activity ?: return ERROR_RETURN

        // Ask if setup was called.
        if (!wasInit()) return ERROR_RETURN

        val notification = try {
             adapter.fromJson(notificationData) ?: return ERROR_RETURN
        } catch (e: Exception) {
            Logger.error("E: $e")
            return ERROR_RETURN
        }

        Logger.debug("Data: $notification")

        val builder = Notification.getBuilder(context, channelId, data=notification) ?: return ERROR_RETURN

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @UsedByGodot
    fun requestPostNotificationsPermissions(): Int {
        if (!wasInit()) return ERROR_RETURN
        val context = activity ?: return ERROR_RETURN
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE_NOTIFICATIONS
        )
        return 0
    }

    private fun wasInit(): Boolean {
        if (!initialized) {
            Logger.error("You must call setup first!")
        }
        return initialized
    }
}
