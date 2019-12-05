package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.token.KyberEnabledEntity
import kotlinx.android.parcel.Parcelize


@Parcelize
data class KyberEnabled(
    val `data`: Boolean = true,
    val success: Boolean = true
) : Parcelable {
    constructor(entity: KyberEnabledEntity) : this(entity.data ?: true, entity.success ?: true)
}