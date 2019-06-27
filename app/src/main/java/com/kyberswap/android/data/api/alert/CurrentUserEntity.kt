package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class CurrentUserEntity(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("email")
    val email: String = "",
    @SerializedName("rank")
    val rank: Int = 0,
    @SerializedName("active_alerts")
    val activeAlerts: List<AlertEntity> = listOf()
)