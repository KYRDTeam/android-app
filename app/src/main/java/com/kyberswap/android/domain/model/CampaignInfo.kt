package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.alert.CampaignInfoEntity
import com.kyberswap.android.util.views.DateTimeHelper


data class CampaignInfo(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val rewardUnit: String = "",
    val reward: String = "",
    val eligibleTokens: String = ""
) {
    constructor(entity: CampaignInfoEntity) : this(
        entity.id,
        entity.title,
        entity.description,
        entity.startTime,
        entity.endTime,
        entity.rewardUnit,
        entity.reward,
        entity.eligibleTokens
    )

    val displayStartTime: String
        get() = DateTimeHelper.displayDate(startTime)

    val displayEndTime: String
        get() = DateTimeHelper.displayDate(endTime)

}