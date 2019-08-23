package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class AlertMethodsEntity(
    @SerializedName("emails")
    val emails: List<EmailEntity> = listOf(),
    @SerializedName("telegram")
    val telegram: TelegramEntity = TelegramEntity()
)