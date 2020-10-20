package com.bc.gordiansigner.service.storage.sharedpref

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bc.gordiansigner.BuildConfig

/**
 * This level of security uses Android Keystore system to encrypt/decrypt the shared preference content.
 * The key never enters to the app process so it's secure even the phone could get hack.
 */
class SecureSharedPref internal constructor(private val context: Context) : SharedPref() {

    override val sharedPreferences: SharedPreferences
        get() = EncryptedSharedPreferences.create(
            BuildConfig.APPLICATION_ID + "_secure",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

}