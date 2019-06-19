package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.toDisplayNumber
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
        entity.status,
        entity.txHash,
        entity.createdAt,
        entity.updatedAt
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


    val destDisplayFee: String
        get() = StringBuilder()
            .append(fee.multiply(srcAmount).toDisplayNumber())
            .append(" ")
            .append(src)
            .toString()

    val isPending: Boolean
        get() = status.toLowerCase() == Status.OPEN.value || status.toLowerCase() == Status.IN_PROGRESS.value

    val displayedDate: String
        get() = formatterShort.format(Date(createdAt * 1000L))

    val displayAddress: String
        get() = userAddr.displayWalletAddress()

    companion object {
        val formatterShort = SimpleDateFormat("dd MMM yyyy", Locale.US)
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

