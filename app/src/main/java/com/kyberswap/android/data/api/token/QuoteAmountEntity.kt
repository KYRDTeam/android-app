package com.kyberswap.android.data.api.token


import com.google.gson.annotations.SerializedName

data class QuoteAmountEntity(
    @SerializedName("error")
    val error: Boolean?,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("additional_data")
    val additionalData: String?,
    @SerializedName("data")
    val `data`: String?
)