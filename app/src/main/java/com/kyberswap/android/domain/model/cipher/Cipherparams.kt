package com.kyberswap.android.domain.model.cipher

import com.google.gson.annotations.SerializedName

data class Cipherparams(
    @SerializedName("iv") val iv: String
)