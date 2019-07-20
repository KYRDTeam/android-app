package com.kyberswap.android.domain.model


import com.kyberswap.android.data.api.alert.LeaderBoardEntity

data class LeaderBoard(
    val currentUser: CurrentUser = CurrentUser(),
    val `data`: List<Alert> = listOf(),
    val campaignInfo: CampaignInfo = CampaignInfo(),
    val lastCampaignTitle: String = ""
) {
    constructor(entity: LeaderBoardEntity) : this(
        CurrentUser(entity.currentUserEntity),
        entity.data.map {
            Alert(it)
        },
        CampaignInfo(entity.campaignInfo),
        entity.lastCampaignTitle ?: ""
    )
}