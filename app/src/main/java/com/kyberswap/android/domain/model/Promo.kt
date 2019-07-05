package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.promo.PromoEntity
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Promo(
    val privateKey: String = "",
    val expiredDate: String = "",
    val destinationToken: String = "",
    val description: String = "",
    val type: String = "",
    val receiveAddress: String = "",
    val error: String? = ""
) : Parcelable {
    constructor(entity: PromoEntity) : this(
        entity.privateKey,
        entity.expiredDate,
        entity.destinationToken,
        entity.description,
        entity.type,
        entity.receiveAddress,
        entity.error
    )
}