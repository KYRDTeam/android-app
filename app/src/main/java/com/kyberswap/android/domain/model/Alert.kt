package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.alert.AlertEntity
import java.math.BigDecimal


data class Alert(
    val id: Int = 0,
    val base: String = "",
    val symbol: String = "",
    val alertType: String = "",
    val alertPrice: BigDecimal = BigDecimal.ZERO,
    val createdAtPrice: BigDecimal = BigDecimal.ZERO,
    val percentChange: BigDecimal = BigDecimal.ZERO,
    val isAbove: Boolean = false,
    val status: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val triggeredAt: Long = 0,
    val filledAt: Long = 0
) {
    constructor(entity: AlertEntity) : this(
        entity.id,
        entity.base,
        entity.symbol,
        entity.alertType,
        entity.createdAtPrice,
        entity.createdAtPrice,
        entity.percentChange,
        entity.isAbove,
        entity.status,
        entity.createdAt,
        entity.updatedAt,
        entity.triggeredAt,
        entity.filledAt
    )

    val pair: String
        get() = StringBuilder()
            .append(symbol)
            .append("/")
            .append(base).toString()
}