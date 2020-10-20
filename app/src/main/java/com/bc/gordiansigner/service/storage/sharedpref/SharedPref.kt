package com.bc.gordiansigner.service.storage.sharedpref

import android.content.SharedPreferences
import com.bc.gordiansigner.helper.ext.newGsonInstance
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass

abstract class SharedPref internal constructor() {

    protected abstract val sharedPreferences: SharedPreferences

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String, type: KClass<T>, default: Any? = null): T {
        return when (type) {
            String::class -> sharedPreferences.getString(
                key,
                default as? String ?: ""
            ) as T
            Boolean::class -> sharedPreferences.getBoolean(
                key,
                default as? Boolean ?: false
            ) as T
            Float::class -> sharedPreferences.getFloat(
                key,
                default as? Float ?: 0f
            ) as T
            Int::class -> sharedPreferences.getInt(
                key,
                default as? Int ?: 0
            ) as T
            Long::class -> sharedPreferences.getLong(
                key,
                default as? Long ?: 0
            ) as T
            Set::class -> sharedPreferences.getStringSet(key, setOf()) as T
            else -> newGsonInstance().fromJson(
                sharedPreferences.getString(key, ""), type.java
            )
        }
    }

    fun <T> put(key: String, data: T) {
        val editor = sharedPreferences.edit()
        when (data) {
            is String -> editor.putString(key, data as String)
            is Boolean -> editor.putBoolean(key, data as Boolean)
            is Float -> editor.putFloat(key, data as Float)
            is Int -> editor.putInt(key, data as Int)
            is Long -> editor.putLong(key, data as Long)
            is Set<*> -> editor.putStringSet(key, data as Set<String>)
            else -> editor.putString(key, newGsonInstance().toJson(data))
        }
        editor.apply()
    }

    fun clear(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}

fun <T> SharedPref.rxSingle(action: (SharedPref) -> T) =
    Single.fromCallable { action(this) }.subscribeOn(Schedulers.io())

fun SharedPref.rxCompletable(action: (SharedPref) -> Unit) =
    Completable.fromCallable { action(this) }.subscribeOn(Schedulers.io())