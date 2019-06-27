package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class LeaderBoardEntity(
    @SerializedName("current_user")
    val currentUserEntity: CurrentUserEntity = CurrentUserEntity(),
    @SerializedName("data")
    val `data`: List<AlertEntity> = listOf(),
    @SerializedName("campaign_info")
    val campaignInfo: CampaignInfoEntity = CampaignInfoEntity(),
    @SerializedName("last_campaign_title")
    val lastCampaignTitle: String? = ""
)