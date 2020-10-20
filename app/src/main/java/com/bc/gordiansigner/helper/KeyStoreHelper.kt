package com.bc.gordiansigner.helper

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Device.aboveP
import com.bc.gordiansigner.helper.Device.aboveR
import com.bc.gordiansigner.helper.ext.enrollDeviceSecurity
import com.bc.gordiansigner.helper.ext.isStrongboxBacked
import com.bc.gordiansigner.helper.ext.openAppSetting
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStoreException
import javax.crypto.KeyGenerator

object KeyStoreHelper {

    const val AUTH_FAILED_CODE = -1
    const val ENROLLMENT_REQUEST_CODE = 0x9A

    @Throws(GeneralSecurityException::class)
    fun generateKey(keyGenParameterSpec: KeyGenParameterSpec): String {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
        return keyGenParameterSpec.keystoreAlias
    }

    @SuppressLint("NewApi")
    fun buildSuperSecureMasterKeySpec(
        context: Context,
        keyAlias: String,
        keySize: Int
    ): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationValidityDurationSeconds(30)
            .setKeySize(keySize)
        if (aboveR()) {
            builder.setUserAuthenticationParameters(30, KeyProperties.AUTH_BIOMETRIC_STRONG)
        }
        if (aboveP() && context.isStrongboxBacked()) {
            builder.setIsStrongBoxBacked(true)
        }
        return builder.build()
    }

    fun biometricAuth(
        activity: FragmentActivity,
        @StringRes title: Int,
        @StringRes message: Int,
        successCallback: () -> Unit,
        failedCallback: (Int) -> Unit
    ) {
        val code = BiometricManager.from(activity).canAuthenticate()
        if (code != BiometricManager.BIOMETRIC_SUCCESS) {
            failedCallback(code)
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(title))
            .setSubtitle(activity.getString(message))
            .setNegativeButtonText(activity.getString(R.string.cancel))
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    successCallback()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    failedCallback(AUTH_FAILED_CODE)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    failedCallback(errorCode)
                }
            })
        biometricPrompt.authenticate(promptInfo)
    }

    fun handleKeyStoreError(
        context: Context,
        throwable: Throwable,
        dialogController: DialogController,
        navigator: Navigator,
        authRequiredCallback: () -> Unit = {}
    ): Boolean {
        return when (throwable) {
            is InvalidKeyException -> {
                dialogController.alert(
                    R.string.error,
                    R.string.your_accounts_are_invalidated,
                    clickEvent = { navigator.openAppSetting(context) }
                )
                true
            }
            is KeyStoreException -> {
                if (throwable.cause is UserNotAuthenticatedException) {
                    authRequiredCallback()
                } else {
                    dialogController.alert(
                        R.string.error,
                        R.string.your_accounts_are_invalidated,
                        clickEvent = { navigator.openAppSetting(context) }
                    )
                }
                true
            }
            is InvalidAlgorithmParameterException -> {
                dialogController.alert(
                    R.string.error,
                    R.string.device_authentication_setup_required
                ) { navigator.anim(RIGHT_LEFT).enrollDeviceSecurity() }
                true
            }
            else -> false
        }
    }
}