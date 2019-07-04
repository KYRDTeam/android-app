package com.kyberswap.android.data.api.user


import com.google.gson.annotations.SerializedName

data class KycResponseStatusEntity(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("reason")
    val reason: List<String> = listOf()
)