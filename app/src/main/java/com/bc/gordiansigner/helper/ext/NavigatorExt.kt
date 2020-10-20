package com.bc.gordiansigner.helper.ext

import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.net.Uri
import android.provider.Settings
import com.bc.gordiansigner.helper.Device.aboveR
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.NONE


fun Navigator.openAppSetting(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        startActivity(intent)
    } catch (ignore: Throwable) {
    }
}

fun Navigator.openSecuritySetting() {
    try {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        startActivity(intent)
    } catch (ignore: Throwable) {
    }
}

fun Navigator.enrollDeviceSecurity() {
    if (aboveR()) {
        val enrollIntent =
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG
                )
            }
        startActivityForResult(
            enrollIntent,
            KeyStoreHelper.ENROLLMENT_REQUEST_CODE
        )
    } else {
        openSecuritySetting()
    }
}

fun Navigator.browseDocument(mime: String = "*/*", requestCode: Int) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = mime
    anim(NONE).startActivityForResult(intent, requestCode)
}

fun Navigator.shareFile(uri: Uri, subject: String = "") {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "application/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        startActivity(
            Intent.createChooser(
                intent,
                subject
            )
        )
    } catch (t: Throwable) {
        //ignore
    }
}