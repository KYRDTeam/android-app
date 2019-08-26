package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.alert.CurrentUserEntity

data class CurrentUser(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("email")
    val email: String = "",
    @SerializedName("rank")
    val rank: Int = 0,
    @SerializedName("active_alerts")
    val activeAlerts: List<Alert> = listOf()
) {
    constructor(entity: CurrentUserEntity) : this(
        entity.id,
        entity.email ?: "",
        entity.rank ?: 0,
        entity.activeAlerts.map {
            Alert(it)

    )
}