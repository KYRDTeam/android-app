package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.SerializedName

data class FeeEntity(
    @SerializedName("fee")
    val fee: Double = 0.0,
    @SerializedName("success")
    val success: Boolean = false
)