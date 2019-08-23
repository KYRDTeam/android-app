package com.kyberswap.android.data.api.token


import com.google.gson.annotations.SerializedName

data class EstimateAmountEntity(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("value")
    val value: String = ""
)