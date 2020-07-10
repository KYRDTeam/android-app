package com.kyberswap.android.data.api.rate


import com.google.gson.annotations.SerializedName

data class ReferencePriceEntity(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("value")
    val value: String?
)