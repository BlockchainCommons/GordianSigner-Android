package com.bc.gordiansigner.helper.ext

import java.text.SimpleDateFormat
import java.util.*

fun Date.toString(
    format: String
): String {
    return try {
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        formatter.format(this)
    } catch (e: Throwable) {
        ""
    }
}