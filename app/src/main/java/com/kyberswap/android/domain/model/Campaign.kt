package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.campaign.CampaignEntity
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Campaign(
    val imageUrl: String,
    val link: String
) : Parcelable {
    constructor(entity: CampaignEntity) : this(entity.imageUrl ?: "", entity.link ?: "")
}