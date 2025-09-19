package org.godotengine.plugin.android.notifications

import android.Manifest
import android.content.Context
import org.json.JSONArray
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import org.godotengine.plugin.android.notifications.types.Notification
import org.godotengine.plugin.android.notifications.types.NotificationData
import org.godotengine.plugin.android.notifications.utils.Logger
import org.godotengine.plugin.android.notifications.utils.ResourceLoader
import org.godotengine.plugin.android.notifications.utils.enums.NotificationError
import kotlin.math.absoluteValue
import kotlin.random.Random

class NotificationHandler(godot: Godot): GodotPlugin(godot) {
    private var initialized: Boolean = false

    companion object {
        const val TOP = 1000
        private val notificationIds: MutableSet<Int> = mutableSetOf()
        val schedule: MutableMap<Int, Any?> = mutableMapOf()

        fun createId(): Int? {
            // Set a limit for new ids.
            if (notificationIds.size > TOP) return null

            var aux: Int = Random.nextInt().absoluteValue
            while (!notificationIds.add(aux)) {
                aux = Random.nextInt()
            }

            return aux
        }

        private fun setResult(data: Map<String, Any?>): String {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any?>>(type)
            return adapter.toJson(data)
        }

        private fun setErrorResult(err: NotificationError): String {
            return setResult(mapOf("err_code" to err.value))
        }

        fun activateNotification(
            context: Context,
            channelId: String,
            data: NotificationData,
            notificationId: Int,
        ): String {
            val builder = Notification.getBuilder(context, channelId, data)
            if (notificationIds.none { it == notificationId }) return setErrorResult(NotificationError.InvalidId)

            return with(NotificationManagerCompat.from(context)) {
                // Just for En Android 13+.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionStatus = ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    )
                    if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                        Logger.error("POST_NOTIFICATIONS permission not granted!")
                        return@with setErrorResult(NotificationError.PermissionNeeded)
                    }
                }

                notify(notificationId, builder.build())
                Logger.debug("Notification sent with ID $notificationId")

                // Returns notificationId to godot.
                setResult(mapOf("notificationId" to notificationId))
            }
        }
    }

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    @UsedByGodot
    fun echo(echoThis: String) {
        runOnUiThread {
            Toast.makeText(activity, echoThis, Toast.LENGTH_LONG).show()
            Logger.debug("$pluginName Echo!")
        }
    }

    @UsedByGodot
    fun setup(
        channelsJson: String,
        imagesJson: String,
    ): String {
        // If activity is null, do nothing
        val context = activity ?: return setErrorResult(NotificationError.NoContext)

        // Set initialized flag.
        this.initialized = true

        // Create the channels provided.
        val channelsArray = getJsonArray(channelsJson) ?: return setErrorResult(NotificationError.JsonError)
        val channelHandler = ChannelHandler(context)

        for (i in 0 until channelsArray.length()) {
            val ch = channelsArray.getJSONObject(i)
            channelHandler.createChannelFromJson(ch)
        }

        // Save images.
        val imagesArray = getJsonArray(imagesJson) ?: return setErrorResult(NotificationError.JsonError)
        for (i in 0 until imagesArray.length()) {
            // Check if image could be loaded.
            val image = imagesArray[i] as String
            val err = ResourceLoader.loadImage(context, image) ?: continue
            return setErrorResult(err)
        }

        Logger.debug("Setup completed!")
        return setResult(mapOf("message" to "The setup was a success"))
    }

    @UsedByGodot
    fun triggerNotification(
        channelId: String,
        notificationData: String,
    ): String {
        Logger.debug("Trigger notification with -> channelId: $channelId | data: $notificationData")

        // If activity is null, do nothing.
        val context = activity ?: return setErrorResult(NotificationError.NoContext)

        // Ask if setup was called.
        if (!initialized) return setErrorResult(NotificationError.NoSetup)

        // Parse data.
        val notification = Notification.parseData(notificationData)
            ?: return setErrorResult(NotificationError.InvalidNotificationData)

        // Creates a new id.
        val newId = createId() ?: return setErrorResult(NotificationError.IDLimit)

        // Trigger notification.
        return activateNotification(context, channelId, notification, newId)
    }

    @UsedByGodot
    fun jsonNotification(
        channelId: String,
        jsonPath: String,
    ): String {
        Logger.debug("JSON: $jsonPath")

        // If activity is null, do nothing.
        val context = activity ?: return setErrorResult(NotificationError.NoContext)

        // Ask if setup was called.
        if (!initialized) return setErrorResult(NotificationError.NoSetup)

        // Parse data.
        val notification = Notification.fromJson(context, jsonPath) ?: return setErrorResult(NotificationError.InvalidNotificationData)

        val newId = createId() ?: return setErrorResult(NotificationError.IDLimit)

        return activateNotification(context, channelId, notification, newId)
    }

    @UsedByGodot
    fun schedule(
        channelId: String,
        notificationData: String,
        seconds: Int,
    ): String {
        Logger.debug("Schedule notification -> channelId: $channelId | data: $notificationData after $seconds seconds")

        val context = activity ?: return setErrorResult(NotificationError.NoContext)
        if (!initialized) return setErrorResult(NotificationError.NoSetup)

        // Parse data.
        val notification = Notification.parseData(notificationData)
            ?: return setErrorResult(NotificationError.InvalidNotificationData)

        val newId = createId() ?: return setErrorResult(NotificationError.IDLimit)

        schedule.put(newId, notification)
        setTimer(seconds.toLong()) { activateScheduledNotification(channelId, newId) }

        return setResult(mapOf("notificationId" to newId))
    }

    @UsedByGodot
    fun scheduleJson(
        channelId: String,
        path: String,
        seconds: Int,
    ): String {
        Logger.debug("Schedule notification -> channelId: $channelId | json: $path after ${seconds.toLong()} seconds")

        val context = activity ?: return setErrorResult(NotificationError.NoContext)
        if (!initialized) return setErrorResult(NotificationError.NoSetup)

        val newId = createId() ?: return setErrorResult(NotificationError.IDLimit)
        schedule.put(newId, path)

        setTimer(seconds.toLong()) { activateScheduledJsonNotification(channelId, newId) }

        return setResult(mapOf("notificationId" to newId))
    }

    fun setTimer(
        seconds: Long,
        block: () -> String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(seconds * 1000)
            block()
        }
    }

    private fun getJsonArray(arr: String): JSONArray? {
        try {
            return JSONArray(arr)
        } catch (e: Exception) {
            Logger.error("Invalid JSON: $arr \n Error: $e")
            return null
        }
    }

    private fun activateScheduledNotification(
        channelId: String,
        notificationId: Int,
    ): String {
        val context = activity ?: return setErrorResult(NotificationError.NoContext)
        val data = schedule[notificationId] as? NotificationData ?: return setErrorResult(NotificationError.InvalidId)
        return activateNotification(context, channelId, data, notificationId)
    }

    private fun activateScheduledJsonNotification(
        channelId: String,
        notificationId: Int,
    ): String {
        val context = activity ?: return setErrorResult(NotificationError.NoContext)
        val path = schedule[notificationId] as? String ?: return setErrorResult(NotificationError.InvalidId)
        val data = Notification.fromJson(context, path) ?: return setErrorResult(NotificationError.JsonError)
        return activateNotification(context, channelId, data, notificationId)
    }
}
