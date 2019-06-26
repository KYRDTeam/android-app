package com.kyberswap.android.domain.model


import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.alert.LeaderBoardEntity

data class LeaderBoard(
    @SerializedName("current_user")
    val currentUserEntity: CurrentUser = CurrentUser(),
    @SerializedName("data")
    val `data`: List<Alert> = listOf(),
    @SerializedName("last_campaign_title")
    val lastCampaignTitle: String = ""
) {
    constructor(entity: LeaderBoardEntity) : this(
        CurrentUser(entity.currentUserEntity),
        entity.data.map {
            Alert(it)
,
        entity.lastCampaignTitle ?: ""
    )
}