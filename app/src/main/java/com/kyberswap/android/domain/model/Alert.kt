package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.views.DateTimeHelper
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

@Entity(tableName = "alerts")
@Parcelize
data class Alert(
    @PrimaryKey
    val id: Long = 0,
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
    val triggeredAt: String = "",
    val filledAt: String = "",
    val userId: Long = 0,
    val rank: Int = 0,
    val userEmail: String = "",
    val telegramAccount: String = "",
    val reward: String = "",
    var message: String? = "",
    val state: String = "",
    @Embedded
    val token: Token = Token(),
    val walletAddress: String = "",
    val userName: String = ""

) : Parcelable {
    constructor(entity: AlertEntity) : this(
        entity.id,
        entity.base,
        entity.symbol,
        entity.alertType,
        entity.alertPrice,
        entity.createdAtPrice,
        entity.percentChange,
        entity.isAbove,
        entity.status,
        entity.createdAt,
        entity.updatedAt,
        entity.triggeredAt ?: "",
        entity.filledAt ?: "",
        entity.userId ?: 0,
        entity.rank ?: 0,
        entity.userEmail ?: "",
        entity.telegramAccount ?: "",
        entity.reward ?: "",
        entity.message ?: ""
    )

    val displayRank: String
        get() = rank.toString()

    val baseInt: Int
        get() = if (base.toLowerCase() === BASE_ETH) 0 else 1

    val isNotLocal: Boolean
        get() = state.toLowerCase() != STATE_LOCAL

    val statusInt: Int
        get() = if (status.toLowerCase() == STATUS_FILLED.toLowerCase()) 1 else 0

    val isFilled: Boolean
        get() = status.toLowerCase() == STATUS_FILLED.toLowerCase()

    val isEthBase: Boolean
        get() = base.toLowerCase() == BASE_ETH.toLowerCase()
    val tokenSymbol: String
        get() = if (token.tokenSymbol.isNotEmpty()) token.tokenSymbol else symbol

    val pair: String
        get() = StringBuilder()
            .append(symbol)
            .append("/")
            .append(base).toString()

    val displayAlertPrice: String
        get() = if (alertPrice != BigDecimal.ZERO) alertPrice.toDisplayNumber() else ""

    val alertPriceWithPrefix: String
        get() = StringBuilder().append(if (isAbove) "≥ " else "≤ ")
            .append(alertPrice.toDisplayNumber()).toString()

    val displayCreatedAtPrice: String
        get() = createdAtPrice.toDisplayNumber()

    val displayPercentChange: String
        get() = StringBuilder()
            .append(percentChange.setScale(2, RoundingMode.UP)?.toDisplayNumber()).append("%")
            .toString()

    val displayTriggerAt: String
        get() = DateTimeHelper.displayDate(triggeredAt)


    companion object {
        const val STATE_LOCAL = "local"
        const val LOCAL_ID = 0L
        const val BASE_USD = "USD"
        const val BASE_ETH = "ETH"
        const val STATUS_ACTIVE = "active"
        const val STATUS_FILLED = "filled"
        const val MAX_ALERT_NUMBER = 10
    }
}