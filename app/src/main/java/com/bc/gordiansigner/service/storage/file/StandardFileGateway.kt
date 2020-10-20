package com.bc.gordiansigner.service.storage.file

import android.content.Context
import java.io.File

class StandardFileGateway internal constructor(context: Context) : FileGateway(context) {

    override fun write(path: String, name: String, data: ByteArray) {
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, name)
        if (!file.exists()) file.createNewFile()
        file.writeBytes(data)
    }

    override fun writeOnFilesDir(name: String, data: ByteArray) =
        write(context.filesDir.absolutePath, name, data)

    override fun read(path: String) = File(path).let { f ->
        if (f.isDirectory) throw IllegalStateException("do not support directory")
        if (f.exists()) {
            f.readBytes()
        } else {
            if (f.createNewFile()) {
                byteArrayOf()
            } else {
                throw IllegalStateException("cannot create new file")
            }
        }
    }

    override fun readOnFilesDir(name: String) = read(File(context.filesDir, name).absolutePath)
}