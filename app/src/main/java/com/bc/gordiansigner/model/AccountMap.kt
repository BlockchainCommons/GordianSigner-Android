package com.bc.gordiansigner.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AccountMap(

    @Expose
    @SerializedName("descriptor")
    val descriptor: String,

    @Expose
    @SerializedName("blockheight")
    val blockheight: String,

    @Expose
    @SerializedName("label")
    val label: String
)