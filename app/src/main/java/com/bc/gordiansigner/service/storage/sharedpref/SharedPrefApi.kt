package com.bc.gordiansigner.service.storage.sharedpref

import android.content.Context
import javax.inject.Inject

class SharedPrefApi @Inject constructor(
    context: Context
) {

    val STANDARD = lazy { StandardSharedPref(context) }.value

    val SECURE = lazy { SecureSharedPref(context) }.value

    val SUPER_SECURE = lazy { SuperSecureSharedPref(context) }.value
}


object SharedPrefKey {
    // Key comes here
}