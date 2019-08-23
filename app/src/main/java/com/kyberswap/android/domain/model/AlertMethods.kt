package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.alert.AlertMethodsEntity

data class AlertMethods(
    @SerializedName("emails")
    val emails: List<Email> = listOf(),
    @SerializedName("telegram")
    val telegram: Telegram = Telegram()
) {
    constructor(entity: AlertMethodsEntity) : this(
        entity.emails.map {
            Email(it)
        },
        Telegram(entity.telegram)
    )
}