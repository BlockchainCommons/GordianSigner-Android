package com.bc.gordiansigner.service.storage.file

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * This level of security uses Android Keystore system to encrypt/decrypt the file content.
 * The key never enters to the app process so it's secure even the phone could get hack.
 */
open class SecureFileGateway internal constructor(
    context: Context
) : FileGateway(context) {

    protected open val MASTER_KEY_ALIAS = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    override fun write(path: String, name: String, data: ByteArray) {
        val file = getEncryptedFile("$path/$name", false)
        file.openFileOutput().apply {
            write(data)
            flush()
            close()
        }
    }

    override fun writeOnFilesDir(name: String, data: ByteArray) {
        write(context.filesDir.absolutePath, name, data)
    }

    override fun read(path: String): ByteArray {
        val file = getEncryptedFile(path, true)
        if (File(path).length() == 0L) return byteArrayOf()
        val inputStream = file.openFileInput()
        val os = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            os.write(nextByte)
            nextByte = inputStream.read()
        }
        return os.toByteArray()
    }

    override fun readOnFilesDir(name: String): ByteArray =
        read(File(context.filesDir, name).absolutePath)

    private fun getEncryptedFile(path: String, read: Boolean) = File(path).let { f ->
        if (f.isDirectory) throw IllegalArgumentException("do not support directory")
        if (read && !f.exists() && !f.createNewFile()) {
            throw IllegalStateException("cannot create new file for reading")
        } else if (!read && f.exists() && !f.delete()) {
            throw IllegalStateException("cannot delete file before writing")
        }
        getEncryptedFileBuilder(f).build()
    }

    protected open fun getEncryptedFileBuilder(f: File) = EncryptedFile.Builder(
        f,
        context,
        MASTER_KEY_ALIAS,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).setKeysetAlias("secure_file_key_set_alias")

}