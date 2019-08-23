package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class AlertResponseEntity(
    @SerializedName("data")
    val alerts: List<AlertEntity> = listOf()
)