package com.bc.gordiansigner.service.storage.sharedpref

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bc.gordiansigner.BuildConfig
import com.bc.gordiansigner.helper.KeyStoreHelper

/**
 * This level of security requires user authentication (device biometric) each time using the key.
 * It also use hardware-backed if API level is 28 or above.
 * Throws [IllegalStateException] if device biometric auth has not been setup.
 * Throws [java.security.KeyStoreException] with root cause [android.security.keystore.UserNotAuthenticatedException] if user has not authenticated by biometric.
 */
class SuperSecureSharedPref internal constructor(private val context: Context) : SharedPref() {

    companion object {
        private const val MASTER_KEY_ALIAS = "super_secure_shared_pref_master_key_alias"
        private const val MASTER_KEY_SIZE = 256
    }

    override val sharedPreferences: SharedPreferences
        get() = EncryptedSharedPreferences.create(
            BuildConfig.APPLICATION_ID + "_super_secure",
            MasterKeys.getOrCreate(
                KeyStoreHelper.buildSuperSecureMasterKeySpec(
                    context,
                    SuperSecureSharedPref.MASTER_KEY_ALIAS,
                    SuperSecureSharedPref.MASTER_KEY_SIZE
                )
            ),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}