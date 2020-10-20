package com.bc.gordiansigner.helper

object Device {
    fun aboveN() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N

    fun aboveP() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P

    fun aboveR() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
}