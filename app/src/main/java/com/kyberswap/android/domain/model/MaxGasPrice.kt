package com.kyberswap.android.domain.model

import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.gas.MaxGasPriceEntity

data class MaxGasPrice(
    @SerializedName("data")
    val `data`: String = "",
    @SerializedName("success")
    val success: Boolean = false
) {
    constructor(entity: MaxGasPriceEntity) : this(entity.data, entity.success)
}