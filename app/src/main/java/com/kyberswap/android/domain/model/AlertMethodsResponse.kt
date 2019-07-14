package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.alert.AlertMethodsResponseEntity

data class AlertMethodsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val `data`: AlertMethods = AlertMethods()
) {
    constructor(entity: AlertMethodsResponseEntity) : this(
        entity.success,
        AlertMethods(entity.data)
    )
}