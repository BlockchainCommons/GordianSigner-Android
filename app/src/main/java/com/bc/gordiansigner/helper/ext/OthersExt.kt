package com.bc.gordiansigner.helper.ext

import android.graphics.Bitmap
import com.google.zxing.common.BitMatrix

fun BitMatrix.toBitmap(size: Int, reverted: Boolean = false): Bitmap {
    val pixels = IntArray(width * height)
    val firstColor = if (reverted) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    val secondColor = if (reverted) 0 else 0xFFFFFFFF.toInt()
    for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] =
                if (get(x, y)) firstColor else secondColor
        }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, size, 0, 0, width, height)
    return bitmap
}