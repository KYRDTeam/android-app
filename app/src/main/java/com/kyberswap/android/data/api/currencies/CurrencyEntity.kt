package com.kyberswap.android.data.api.currencies

import com.google.gson.annotations.SerializedName

data class CurrencyEntity(
    @SerializedName("data")
    val `data`: List<TokenCurrencyEntity> = listOf(),
    @SerializedName("success")
    val success: Boolean = false
)