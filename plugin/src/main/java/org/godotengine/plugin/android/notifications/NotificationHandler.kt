package org.godotengine.plugin.android.notifications

import android.Manifest
import android.util.Log
import org.json.JSONArray
import android.widget.Toast
import android.content.pm.PackageManager
import android.graphics.Bitmap

import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import org.godotengine.plugin.android.notifications.utils.ResourceLoader

class NotificationHandler(godot: Godot): GodotPlugin(godot) {
    private var initialized: Boolean = false

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
        smallIconPath: String,
    ) {
        val context = activity ?: return

        if (!initialized) {
            Log.e(BuildConfig.GODOT_PLUGIN_NAME, "You must call setup first!")
            return
        }

        Log.d(
            BuildConfig.GODOT_PLUGIN_NAME,
            listOf(
                "Channel ID: $channelId",
                "Title: $title",
                "Content: $content",
                "Icon Path: $smallIconPath"
            ).joinToString(separator = " | ")
        )

        val loader: ResourceLoader = ResourceLoader(context)

        val smallIconBitmap: Bitmap = loader.loadImage(smallIconPath) ?: return
        Log.d(BuildConfig.GODOT_PLUGIN_NAME, "Bitmap: $smallIconBitmap")

        val smallIcon: IconCompat = IconCompat.createWithBitmap(smallIconBitmap)
        Log.d(BuildConfig.GODOT_PLUGIN_NAME, "Icon: $smallIcon")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            Log.d(BuildConfig.GODOT_PLUGIN_NAME, "NotificationManagerCompat obtained")

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(BuildConfig.GODOT_PLUGIN_NAME, "POST_NOTIFICATIONS permission not granted")
                return@with
            }

            notify(100, builder.build())
            Log.d(BuildConfig.GODOT_PLUGIN_NAME, "Notification sent with ID 100")
        }
    }
}
