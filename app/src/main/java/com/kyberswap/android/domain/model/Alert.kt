package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

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
    val rank: Int = 0,
    val userEmail: String = "",
    val telegramAccount: String = "",
    val reward: String = "",
    val state: String = "",
    @Embedded
    val token: Token = Token(),
    val walletAddress: String = ""

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
        entity.rank ?: 0,
        entity.userEmail ?: "",
        entity.telegramAccount ?: "",
        entity.reward ?: ""
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
        get() = if (symbol.isNotEmpty()) symbol else token.tokenSymbol

    val pair: String
        get() = StringBuilder()
            .append(symbol)
            .append("/")
            .append(base).toString()

    val displayAlertPrice: String
        get() = if (alertPrice != BigDecimal.ZERO) alertPrice.toDisplayNumber() else ""

    val displayCreatedAtPrice: String
        get() = createdAtPrice.toDisplayNumber()

    val displayPercentChange: String
        get() = StringBuilder()
            .append(percentChange.setScale(2, RoundingMode.UP)?.toDisplayNumber()).append("%")
            .toString()

    val displayTriggerAt: String
        get() = try {
            displayFormat.format(fullFormat.parse(triggeredAt))
 catch (ex: Exception) {
            ex.printStackTrace()
            triggeredAt



    companion object {
        const val STATE_LOCAL = "local"
        const val LOCAL_ID = 0L
        const val BASE_USD = "USD"
        const val BASE_ETH = "ETH"
        const val STATUS_ACTIVE = "active"
        const val STATUS_FILLED = "filled"
        val displayFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US)
        val fullFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    }
}