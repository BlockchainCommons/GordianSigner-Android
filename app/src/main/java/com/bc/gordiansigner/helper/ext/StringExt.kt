package com.bc.gordiansigner.helper.ext

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

fun CharSequence.replaceSpaces() = this.replace("\\s+".toRegex(), " ").trim()

fun String.toQrCode(size: Int, isReverted: Boolean = false): Bitmap {
    val writer = MultiFormatWriter()
    val hints = mapOf(
        Pair(EncodeHintType.MARGIN, 1)
    )
    val bitMatrix = writer.encode(this, BarcodeFormat.QR_CODE, size, size, hints)
    return bitMatrix.toBitmap(size, isReverted)
}