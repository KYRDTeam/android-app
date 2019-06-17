package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.SerializedName

data class NonceEntity(
    @SerializedName("nonce")
    val nonce: String = ""
)