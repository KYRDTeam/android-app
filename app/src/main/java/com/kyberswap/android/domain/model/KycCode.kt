package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName

data class KycCode(
    @SerializedName("data")
    val `data`: Map<String, String> = mapOf()
)