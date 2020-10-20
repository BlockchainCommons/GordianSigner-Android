package com.bc.gordiansigner.service.storage.file

import android.content.Context
import androidx.security.crypto.MasterKeys
import com.bc.gordiansigner.helper.KeyStoreHelper
import java.io.File

/**
 * This level of security requires user authentication (device biometric) each time using the key.
 * It also use hardware-backed if API level is 28 or above.
 * Throws [IllegalStateException] if device biometric auth has not been setup.
 * Throws [java.security.KeyStoreException] with root cause [android.security.keystore.UserNotAuthenticatedException] if user has not authenticated by biometric.
 */
class SuperSecureFileGateway internal constructor(context: Context) : SecureFileGateway(context) {

    override val MASTER_KEY_ALIAS: String
        get() = MasterKeys.getOrCreate(
            KeyStoreHelper.buildSuperSecureMasterKeySpec(
                context,
                "super_secure_file_master_key_alias",
                256
            )
        )

    override fun getEncryptedFileBuilder(f: File) =
        super.getEncryptedFileBuilder(f).setKeysetAlias("super_secure_file_key_set_alias")
}