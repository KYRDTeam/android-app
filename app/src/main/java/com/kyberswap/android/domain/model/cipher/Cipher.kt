package com.kyberswap.android.domain.model.cipher

import com.google.gson.annotations.SerializedName

data class Cipher(
    @SerializedName("id") val id: String,
    @SerializedName("version") val version: Int,
    @SerializedName("crypto") val crypto: Crypto,
    @SerializedName("address") val address: String
)