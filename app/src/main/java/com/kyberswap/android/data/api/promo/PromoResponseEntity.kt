package com.kyberswap.android.data.api.promo


import com.google.gson.annotations.SerializedName

data class PromoResponseEntity(
    @SerializedName("data")
    val `data`: PromoEntity = PromoEntity()
)