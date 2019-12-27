package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.notification.NotificationExtEntity
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class NotificationExt(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("order_id")
    val orderId: Long = 0,
    @SerializedName("src_token")
    val srcToken: String = "",
    @SerializedName("dst_token")
    val dstToken: String = "",
    @SerializedName("min_rate")
    val minRate: BigDecimal = BigDecimal.ZERO,
    @SerializedName("src_amount")
    val srcAmount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("fee")
    val fee: BigDecimal = BigDecimal.ZERO,
    @SerializedName("transfer_fee")
    val transferFee: BigDecimal = BigDecimal.ZERO,
    @SerializedName("receive")
    val receive: BigDecimal = BigDecimal.ZERO,
    @SerializedName("sender")
    val sender: String = "",
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("updated_at")
    val updatedAt: Long = 0,
    @SerializedName("tx_hash")
    val txHash: String = "",
    @SerializedName("alert_id")
    val alertId: Long = 0L,
    @SerializedName("base")
    val base: String = "",
    @SerializedName("token")
    val token: String = "",
    val percent: String = "",
    val link: String = "",
    @SerializedName("notification_id")
    val notificationId: Long = 0L
) : Parcelable {
    constructor(entity: NotificationExtEntity) : this(
        entity.type ?: "",
        entity.orderId ?: 0,
        entity.srcToken ?: "",
        entity.dstToken ?: "",
        entity.minRate ?: BigDecimal.ZERO,
        entity.srcAmount ?: BigDecimal.ZERO,
        entity.fee ?: BigDecimal.ZERO,
        entity.transferFee ?: BigDecimal.ZERO,
        entity.receive ?: BigDecimal.ZERO,
        entity.sender ?: "",
        entity.createdAt ?: 0L,
        entity.updatedAt ?: 0L,
        entity.txHash ?: "",
        entity.alertId ?: 0L,
        entity.base ?: "",
        entity.token ?: "",
        entity.percent ?: ""
    )

    val hasLink: Boolean
        get() = link.isNotEmpty()

    val isBigSwing: Boolean
        get() = type.equals(Notification.TYPE_BIG_SWING, true)

    val isNewListing: Boolean
        get() = type.equals(Notification.TYPE_NEW_LISTING, true)

    val isPromotion: Boolean
        get() = type.equals(Notification.TYPE_PROMOTION, true)

    val isOther: Boolean
        get() = type.equals(Notification.TYPE_OTHER, true)
}