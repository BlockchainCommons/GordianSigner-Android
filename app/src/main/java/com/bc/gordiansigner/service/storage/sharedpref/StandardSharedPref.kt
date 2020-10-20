package com.bc.gordiansigner.service.storage.sharedpref

import android.content.Context
import android.content.SharedPreferences
import com.bc.gordiansigner.BuildConfig

class StandardSharedPref internal constructor(
    context: Context,
    override val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        BuildConfig.APPLICATION_ID,
        Context.MODE_PRIVATE
    )
) : SharedPref()