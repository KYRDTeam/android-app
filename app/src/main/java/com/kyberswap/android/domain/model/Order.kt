package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.data.api.limitorder.OrderEntity
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Entity(tableName = "orders")
@Parcelize
data class Order(
    @PrimaryKey
    val id: Int = 0,
    val userAddr: String = "",
    val src: String = "",
    val dst: String = "",
    val srcAmount: BigDecimal = BigDecimal.ZERO,
    val minRate: BigDecimal = BigDecimal.ZERO,
    val destAddr: String = "",
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
        entity.destAddr,
        entity.nonce,
        entity.fee,
        entity.status,
        entity.txHash,
        entity.createdAt,
        entity.updatedAt
    )
}