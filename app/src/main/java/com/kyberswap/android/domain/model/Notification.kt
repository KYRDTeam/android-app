package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.notification.NotificationEntity
import com.kyberswap.android.data.api.notification.NotificationExtEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Notification(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val scope: String = "",
    val label: String = "",
    val link: String = "",
    val read: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",
    val userId: Long = 0,
    val `data`: NotificationExt = NotificationExt()
) : Parcelable {
    constructor(entity: NotificationEntity) : this(
        entity.id ?: 0,
        entity.title ?: "",
        entity.content ?: "",
        entity.scope ?: "",
        entity.label ?: "",
        entity.link ?: "",
        entity.read ?: false,
        entity.createdAt ?: "",
        entity.updatedAt ?: "",
        entity.userId ?: 0,
        NotificationExt(entity.data ?: NotificationExtEntity())
    )

    constructor(alert: NotificationAlert) : this(alert.notificationId)

    constructor(notificationLimitOrder: NotificationLimitOrder) : this(notificationLimitOrder.notificationId)

    val hasLink: Boolean
        get() = link.isNotEmpty()

    val isBigSwing: Boolean
        get() = label.equals(TYPE_BIG_SWING, true)

    val isNewListing: Boolean
        get() = label.equals(TYPE_NEW_LISTING, true)

    val isPromotion: Boolean
        get() = label.equals(TYPE_PROMOTION, true)

    val isOther: Boolean
        get() = label.equals(TYPE_OTHER, true)

    companion object {
        const val TYPE_PROMOTION = "promotion"
        const val TYPE_NEW_LISTING = "new_listing"
        const val TYPE_BIG_SWING = "big_swing"
        const val TYPE_ALERT = "alert"
        const val TYPE_LIMIT_ORDER = "limit_order"
        const val TYPE_OTHER = "other"
    }
}
