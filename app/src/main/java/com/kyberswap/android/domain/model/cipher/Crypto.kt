package com.kyberswap.android.domain.model.cipher

import com.google.gson.annotations.SerializedName

data class Crypto(
    @SerializedName("ciphertext") val cipherText: String,
    @SerializedName("mac") val mac: String,
    @SerializedName("cipher") val cipher: String,
    @SerializedName("cipherparams") val cipherParams: Cipherparams,
    @SerializedName("kdf") val kdf: String,
    @SerializedName("kdfparams") val kdfParams: Kdfparams
)