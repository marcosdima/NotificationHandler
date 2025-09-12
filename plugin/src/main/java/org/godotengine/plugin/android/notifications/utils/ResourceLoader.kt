package org.godotengine.plugin.android.notifications.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class ResourceLoader(val context: Context) {
    /**
     * Load an image from godot assets. (SVG files aren't supported)
     * @param imageName
     */
    fun loadImage(imageName: String): Bitmap? {
        val baseDir = context.filesDir.absolutePath
        val imageFile = File(baseDir, imageName)

        if (!imageFile.exists()) return null

        return imageFile.inputStream().use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }
}
