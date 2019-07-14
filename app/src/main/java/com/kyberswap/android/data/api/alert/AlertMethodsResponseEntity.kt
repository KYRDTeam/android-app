package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class AlertMethodsResponseEntity(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val `data`: AlertMethodsEntity = AlertMethodsEntity()
)