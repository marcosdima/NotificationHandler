package org.godotengine.plugin.android.notifications.types

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.moshi.Moshi.Builder
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.godotengine.plugin.android.notifications.utils.Logger
import org.godotengine.plugin.android.notifications.utils.ResourceLoader
import org.godotengine.plugin.android.notifications.utils.enums.NotificationError
import org.godotengine.plugin.android.notifications.utils.enums.PriorityAdapter
import java.io.File

object Notification {
    private val adapter = Notification.getMoshiBuilder().build().adapter(NotificationData::class.java)

    fun getMoshiBuilder(): Builder {
        return Builder()
            .add(KotlinJsonAdapterFactory())
            .add(PriorityAdapter())
    }

    fun getBuilder(
        context: Context,
        channelId: String,
        data: NotificationData
    ): NotificationCompat.Builder {
        val smallIconBitmap: Bitmap = ResourceLoader.getImage(data.smallIcon)
        val smallIcon: IconCompat = IconCompat.createWithBitmap(smallIconBitmap)

        val base = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(data.title)
            .setContentText(data.content)
            .setPriority(data.priority.value)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (!data.image.isEmpty()) {
            base.setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigPicture(ResourceLoader.getImage(data.image))
            )
        }
        else if (!data.bigText.isEmpty()) {
            base.setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(data.bigText)
            )
        }
        else if (!data.lines.isEmpty()) {
            val style = NotificationCompat.InboxStyle()
            data.lines.forEach { style.addLine(it) }
            base.setStyle(style)
        }

        return base
    }

    fun parseData(data: String): NotificationData? {
        return try {
            adapter.fromJson(data)
        } catch (e: Exception) {
            Logger.error("E: $e")
            null
        }
    }

    fun fromJson(context: Context, jsonPath: String): NotificationData? {
        return try {
            val file = File(context.filesDir.absolutePath, jsonPath)
            adapter.fromJson(file.readText())
        } catch (e: Exception) {
            Logger.error("E: $e")
            null
        }
    }
}