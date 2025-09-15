package org.godotengine.plugin.android.notifications.types.data

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.moshi.Moshi.Builder
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.godotengine.plugin.android.notifications.utils.Logger
import org.godotengine.plugin.android.notifications.utils.Priority
import org.godotengine.plugin.android.notifications.utils.ResourceLoader

sealed class NotificationData {
    open val title: String = ""
    open val content: String = ""
    open val smallIcon: String = ""
    open val priority: Priority = Priority.DEFAULT

    companion object {
        fun getMoshiBuilder(): Builder {
            return Builder()
                .add(
                    PolymorphicJsonAdapterFactory.of(NotificationData::class.java, "type")
                        .withSubtype(BasicNotification::class.java, "basic")
                        .withSubtype(NotificationWithImage::class.java, "with_image")
                )
                .add(KotlinJsonAdapterFactory())
        }
    }

    open fun getBuilder(context: Context, channelId: String): NotificationCompat.Builder? {
        val smallIconBitmap: Bitmap = ResourceLoader.loadImage(context, this.smallIcon) ?: return null
        val smallIcon: IconCompat = IconCompat.createWithBitmap(smallIconBitmap)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(this.title)
            .setContentText(this.content)
            .setPriority(this.priority.value)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
    }
}

data class BasicNotification(
    override val title: String = "",
    override val content: String = "",
    override val smallIcon: String = "",
    override val priority: Priority = Priority.DEFAULT,
) : NotificationData()


data class NotificationWithImage(
    override val title: String = "",
    override val content: String = "",
    override val smallIcon: String = "",
    override val priority: Priority = Priority.DEFAULT,
    val image: String = ""
) : NotificationData() {
    override fun getBuilder(
        context: Context,
        channelId: String,
    ): NotificationCompat.Builder? {
        val imageBitMap: Bitmap = ResourceLoader.loadImage(context, this.image) ?: return null
        return super.getBuilder(context, channelId)
            ?.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitMap)
            )
    }
}