package com.kyberswap.android.data.api.token


import com.google.gson.annotations.SerializedName

data class KyberEnabledEntity(
    @SerializedName("data")
    val `data`: Boolean?,
    @SerializedName("success")
    val success: Boolean?
)