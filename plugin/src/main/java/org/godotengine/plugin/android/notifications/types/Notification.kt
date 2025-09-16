package org.godotengine.plugin.android.notifications.types

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.moshi.Moshi.Builder
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.godotengine.plugin.android.notifications.utils.ResourceLoader

object Notification {
    fun getMoshiBuilder(): Builder {
        return Builder().add(KotlinJsonAdapterFactory())
    }

    fun getBuilder(context: Context, channelId: String, data: NotificationData): NotificationCompat.Builder? {
        val smallIconBitmap: Bitmap = ResourceLoader.loadImage(context, data.smallIcon) ?: return null
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
                    .bigPicture(
                        ResourceLoader.loadImage(context, data.image)
                    )
            )
        } else if (!data.bigText.isEmpty()) {
            base.setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(data.bigText)
            )
        } else if (!data.lines.isEmpty()) {
            val style = NotificationCompat.InboxStyle()
            data.lines.forEach { style.addLine(it) }
            base.setStyle(style)
        }

        return base
    }
}