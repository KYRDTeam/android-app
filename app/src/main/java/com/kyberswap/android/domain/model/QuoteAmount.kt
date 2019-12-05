package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.token.QuoteAmountEntity
import kotlinx.android.parcel.Parcelize


@Parcelize
data class QuoteAmount(
    val error: Boolean = false,
    val reason: String = "",
    val additionalData: String = "",
    val `data`: String = ""
) : Parcelable {
    constructor(entity: QuoteAmountEntity) : this(
        entity.error ?: false,
        entity.reason ?: "",
        entity.additionalData ?: "",
        entity.data ?: ""
    )
}