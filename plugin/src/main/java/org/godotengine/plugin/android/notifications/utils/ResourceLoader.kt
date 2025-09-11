package org.godotengine.plugin.android.notifications.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.caverock.androidsvg.SVG
import android.util.Log
import org.godotengine.plugin.android.notifications.BuildConfig


class ResourceLoader(val context: Context) {
    /**
     * Load an image from godot assets.
     * @param godotPath ex: "res://static/images/image.png"
     */
    fun loadImage(godotPath: String): Bitmap? {
        val assetPath = godotPath.removePrefix("res://")
        val is_svg = assetPath.endsWith(".svg", ignoreCase = true)
        return try {
            if (is_svg) {
                // SVG
                val svg = SVG.getFromAsset(context.assets, assetPath)
                val width = svg.documentWidth.toInt().coerceAtLeast(1)
                val height = svg.documentHeight.toInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                svg.renderToCanvas(canvas)
                bitmap
            } else {
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
        } catch (e: Exception) {
            Log.e(BuildConfig.GODOT_PLUGIN_NAME, "Bitmap could not be created from: $godotPath", e)
            null
        }
    }

}
