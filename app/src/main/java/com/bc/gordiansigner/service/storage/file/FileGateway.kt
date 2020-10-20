package com.bc.gordiansigner.service.storage.file

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File

abstract class FileGateway(protected val context: Context) {

    abstract fun write(path: String, name: String, data: ByteArray)

    abstract fun writeOnFilesDir(name: String, data: ByteArray)

    abstract fun read(path: String): ByteArray

    abstract fun readOnFilesDir(name: String): ByteArray

    fun isExisting(path: String): Boolean = File(path).exists()

    fun isExistingOnFilesDir(name: String): Boolean =
        isExisting(File(context.filesDir, name).absolutePath)

    fun delete(path: String): Boolean = File(path).let { file ->
        if (!file.exists()) true
        else if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    fun deleteOnFilesDir(name: String): Boolean = delete(File(context.filesDir, name).absolutePath)
}

fun <T> FileGateway.rxSingle(action: (FileGateway) -> T) =
    Single.fromCallable { action(this) }.subscribeOn(Schedulers.io())

fun FileGateway.rxCompletable(action: (FileGateway) -> Unit) =
    Completable.fromCallable { action(this) }.subscribeOn(Schedulers.io())