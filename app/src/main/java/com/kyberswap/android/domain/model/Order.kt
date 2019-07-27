package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toLongSafe
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

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
    val status: String = "",
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
        entity.fee,
        entity.receive,
        entity.status,
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
        notification.fee,
        notification.receive,
        Status.FILLED.value,
        notification.txHash,
        notification.createdAt,
        notification.updatedAt
    )

    val receivedSource: BigDecimal
        get() = (BigDecimal.ONE - fee).multiply(srcAmount)

    val displayTokenPair: String
        get() = StringBuilder()
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
            .append(receivedSource.multiply(minRate).toDisplayNumber())
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
            .append(fee.multiply(srcAmount).toDisplayNumber())
            .append(" ")
            .append(src)
            .toString()

    private val extra: BigDecimal
        get() = receive.minus((srcAmount - fee).multiply(minRate))

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
        get() = extraDisplay.isNotEmpty()


    val isPending: Boolean
        get() = status.toLowerCase() == Status.OPEN.value || status.toLowerCase() == Status.IN_PROGRESS.value

    val isOpen: Boolean
        get() = status.toLowerCase() == Status.OPEN.value

    val isMined: Boolean
        get() = status.toLowerCase() == Status.FILLED.value

    val displayedDate: String
        get() = formatterShort.format(Date(createdAt * 1000L))

    val displayAddress: String
        get() = userAddr.displayWalletAddress()

    companion object {
        val formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.US)
    }

    fun sameDisplay(other: Order): Boolean {
        return this.id == other.id &&
            this.displayTokenPair == other.displayTokenPair &&
            this.displayedDate == other.displayedDate &&
            this.displayAddress == other.displayAddress &&
            this.status == other.status &&
            this.sourceDisplay == other.sourceDisplay &&
            this.destDisplay == other.destDisplay &&
            this.destDisplayFee == other.destDisplayFee


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

