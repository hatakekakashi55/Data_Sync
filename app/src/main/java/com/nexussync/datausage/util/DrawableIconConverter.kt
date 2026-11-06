package com.nexussync.datausage.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object DrawableIconConverter {
    fun toImageBitmap(drawable: Drawable, targetSizePx: Int = 96): ImageBitmap? {
        return runCatching {
            if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                val src = drawable.bitmap
                if (src.width <= targetSizePx && src.height <= targetSizePx) {
                    return@runCatching src.asImageBitmap()
                }
            }
            val width = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
            val height = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }
}
