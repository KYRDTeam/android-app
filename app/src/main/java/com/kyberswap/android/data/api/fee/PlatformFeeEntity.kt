package com.kyberswap.android.data.api.fee


import com.google.gson.annotations.SerializedName

data class PlatformFeeEntity(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("fee")
    val fee: Int?
)