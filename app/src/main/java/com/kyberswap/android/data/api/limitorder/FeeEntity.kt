package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.SerializedName

data class FeeEntity(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("fee")
    val fee: Double = 0.0,
    @SerializedName("discount_percent")
    val discountPercent: Double = 0.0,
    @SerializedName("non_discounted_fee")
    val nonDiscountedFee: Double = 0.0,
    @SerializedName("transfer_fee")
    val transferFee: Double = 0.0
)