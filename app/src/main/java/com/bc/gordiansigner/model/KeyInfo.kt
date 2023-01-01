package com.bc.gordiansigner.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class KeyInfo(

    @Expose
    @SerializedName("fingerprint")
    val fingerprint: String,

    @Expose
    @SerializedName("alias")
    var alias: String,

    @Expose
    @SerializedName("last_used")
    var lastUsed: Date?,

    @Expose
    @SerializedName("is_saved")
    var isSaved: Boolean
): Parcelable {

    companion object {
        fun newEmptyInstance() = KeyInfo("", "", null, false)
        fun newUnknownInstance(fingerprint: String) = KeyInfo(fingerprint, "unknown", null, false)
        fun newDefaultInstance(fingerprint: String, alias: String, isSaved: Boolean) = KeyInfo(fingerprint, alias, Date(), isSaved)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyInfo

        if (fingerprint != other.fingerprint) return false
        return true
    }

    override fun hashCode(): Int {
        return fingerprint.hashCode()
    }

    fun isEmpty() = fingerprint.isEmpty()
}