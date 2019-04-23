package com.kyberswap.android.domain.model.cipher

import com.google.gson.annotations.SerializedName

data class Kdfparams(

    @SerializedName("dklen") val dkLen: Int,
    @SerializedName("c") val c: Int,
    @SerializedName("prf") val prf: String,
    @SerializedName("salt") val salt: String
)