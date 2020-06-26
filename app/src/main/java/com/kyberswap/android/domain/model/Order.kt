package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.shortenValue
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toLongSafe
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "orders")
@Parcelize
data class Order(
    @PrimaryKey
    val id: Long = 0,
    val userAddr: String = "",
    val src: String = "",
    val dst: String = "",
    val srcAmount: BigDecimal = BigDecimal.ZERO,
    val minRate: BigDecimal = BigDecimal.ZERO,
    val nonce: String = "",
    val fee: BigDecimal = BigDecimal.ZERO,
    val receive: BigDecimal = BigDecimal.ZERO,
    val sideTrade: String = "",
    val status: String = "",
    val msg: String = "",
    val txHash: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0

) : Parcelable {
    constructor(entity: OrderEntity) : this(
        entity.id,
        entity.userAddr,
        entity.src,
        entity.dst,
        entity.srcAmount,
        entity.minRate,
        entity.nonce,
        entity.fee + entity.transferFee,
        entity.receive,
        entity.sideTrade,
        entity.status,
        entity.msg,
        entity.txHash,
        entity.createdAt,
        entity.updatedAt
    )

    val time: Long
        get() = if (updatedAt > 0) updatedAt else createdAt

    val shortedDateTimeFormat: String
        get() = Transaction.formatterShort.format(Date(time * 1000L))

    constructor(notification: NotificationLimitOrder) : this(
        if (notification.orderId > 0) notification.orderId else notification.testOrderId.toLongSafe(),
        notification.sender,
        notification.srcToken,
        notification.dstToken,
        notification.srcAmount,
        notification.minRate,
        "",
        notification.fee + notification.transferFee,
        notification.receive,
        notification.sideTrade,
        Status.FILLED.value,
        "",
        notification.txHash,
        notification.createdAt,
        notification.updatedAt
    )

    val receivedSource: BigDecimal
        get() = (BigDecimal.ONE - fee).multiply(srcAmount)

    val displayBuySellToken: String
        get() = if (sideTrade.equals(SIDE_TRADE_BUY, true))
            dst else src

    val displayPrice: String
        get() = if (sideTrade.equals(SIDE_TRADE_BUY, true))
            BigDecimal.ONE.divide(
                minRate,
                10,
                RoundingMode.CEILING
            ).toDisplayNumber()
        else minRate.toDisplayNumber()

    val displayTotal: String
        get() = if (sideTrade.equals(SIDE_TRADE_BUY, true)) sourceDisplay else destDisplay

    val displayAmount: String
        get() = if (sideTrade.equals(SIDE_TRADE_BUY, true)) destDisplay else sourceDisplay

    val isV1: Boolean
        get() = sideTrade.isEmpty()

    val isSell: Boolean
        get() = sideTrade.equals(SIDE_TRADE_SELL, true)

    val isBuy: Boolean
        get() = sideTrade.equals(SIDE_TRADE_BUY, true)

    val displayTokenPair: String
        get() =
            StringBuilder()
                .append(src)
                .append("/")
                .append(dst)
                .append(" >= ")
                .append(minRate.toDisplayNumber())
                .toString()

    val sourceDisplay: String
        get() = StringBuilder()
            .append(srcAmount.toDisplayNumber())
            .append(" ")
            .append(src)
            .toString()

    val destDisplay: String
        get() = StringBuilder()
            .append(receivedSource.multiply(minRate, MathContext(10)).toDisplayNumber())
            .append(" ")
            .append(dst)
            .toString()

    val receviedDisplay: String
        get() = StringBuilder()
            .append(receive.toDisplayNumber())
            .append(" ")
            .append(dst)
            .toString()

    val destDisplayFee: String
        get() = StringBuilder()
            .append(fee.multiply(srcAmount).toDisplayNumber().exactAmount())
            .append(" ")
            .append(src)
            .toString()

    private val extra: BigDecimal
        get() = receive.minus(receivedSource.multiply(minRate))

    private val extraValue: String
        get() = if (extra > BigDecimal.ZERO) extra.toDisplayNumber() else ""

    val extraDisplay: String
        get() = if (extraValue.isNotEmpty())
            StringBuilder().append(
                "+ "
            ).append(extraValue)
                .append(" ")
                .append(dst)
                .toString()
        else ""

    val hasExtra: Boolean
        get() = isMined && extraDisplay.isNotEmpty()

    val isInvalidated: Boolean
        get() = status.toLowerCase(Locale.getDefault()) == Status.INVALIDATED.value.toLowerCase(
            Locale.getDefault()
        ) &&
            msg.isNotEmpty()

    val hasErrorMessage: Boolean
        get() = msg.isNotEmpty()

    val isPending: Boolean
        get() = status.toLowerCase(Locale.getDefault()) == Status.OPEN.value

    val isOpen: Boolean
        get() = status.toLowerCase(Locale.getDefault()) == Status.OPEN.value || status.toLowerCase(
            Locale.getDefault()
        ) == Status.IN_PROGRESS.value

    val isMined: Boolean
        get() = status.toLowerCase(Locale.getDefault()) == Status.FILLED.value

    val displayedDate: String
        get() = formatterShort.format(Date(time * 1000L))

    val displayAddress: String
        get() = userAddr.shortenValue()

    companion object {
        val formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.US)
        const val SIDE_TRADE_BUY = "buy"
        const val SIDE_TRADE_SELL = "sell"
    }

    fun sameDisplay(other: Order): Boolean {
        if (isV1) {
            return this.id == other.id &&
                this.displayTokenPair.equals(other.displayTokenPair, true) &&
                this.displayAddress.equals(other.displayAddress, true) &&
                this.status.equals(other.status, true) &&
                this.sourceDisplay.equals(other.sourceDisplay, true) &&
                this.destDisplay.equals(other.destDisplay, true) &&
                this.destDisplayFee.equals(other.destDisplayFee, true) &&
                this.extraDisplay.equals(other.extraDisplay, true) &&
                this.msg.equals(other.msg, true)
        } else {
            return this.id == other.id &&
                this.displayTokenPair.equals(other.displayTokenPair, true) &&
                this.displayAddress.equals(other.displayAddress, true) &&
                this.status.equals(other.status, true) &&
                this.sideTrade.equals(other.sideTrade, true) &&
                this.displayPrice.equals(other.displayPrice, true) &&
                this.displayTotal.equals(other.displayTotal, true) &&
                this.displayAmount.equals(other.displayAmount, true) &&
                this.destDisplayFee.equals(other.destDisplayFee, true) &&
                this.extraDisplay.equals(other.extraDisplay, true) &&
                this.msg.equals(other.msg, true) &&
                this.hasExtra == other.hasExtra
        }
    }

    enum class Status {
        OPEN,
        IN_PROGRESS,
        CANCELLED,
        FILLED,
        INVALIDATED,
        UNKNOWN;

        val value: String
            get() = when (this) {
                OPEN -> "open"
                IN_PROGRESS -> "in_progress"
                CANCELLED -> "cancelled"
                FILLED -> "filled"
                INVALIDATED -> "invalidated"
                UNKNOWN -> "unknown"
            }
    }
}

