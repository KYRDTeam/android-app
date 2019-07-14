package com.kyberswap.android.domain.model


import com.kyberswap.android.data.api.alert.TelegramEntity

data class Telegram(
    val id: String = "",
    val active: Boolean = false,
    val name: String = ""
) {
    constructor(entity: TelegramEntity) : this(entity.id, entity.active, entity.name)
}