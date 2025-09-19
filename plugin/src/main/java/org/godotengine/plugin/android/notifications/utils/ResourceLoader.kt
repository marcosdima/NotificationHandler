package org.godotengine.plugin.android.notifications.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.godotengine.plugin.android.notifications.utils.enums.NotificationError
import java.io.File

object ResourceLoader {
    val imagesBitMap: MutableMap<String, Bitmap> = mutableMapOf<String, Bitmap>()

    /**
     * Load an image from godot assets. (SVG files aren't supported)
     * @param imageName
     */
    fun loadImage(context: Context, imageName: String): NotificationError? {
        val baseDir = context.filesDir.absolutePath
        val imageFile = File(baseDir, imageName)

        Logger.debug("Loading: $imageName - Exists: ${imageFile.exists()}")
        if (!imageFile.exists()) return NotificationError.ImageNotFound

        // Save bitmap
        imagesBitMap[imageName] = imageFile.inputStream().use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }

        return null
    }

    /**
     * Get an image from imagesBitMap.
     * @param k Image key
     */
    fun getImage(k: String): Bitmap = imagesBitMap[k] ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}
