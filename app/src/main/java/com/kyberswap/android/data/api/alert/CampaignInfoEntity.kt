package com.kyberswap.android.data.api.alert


import com.google.gson.annotations.SerializedName

data class CampaignInfoEntity(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("start_time")
    val startTime: String = "",
    @SerializedName("end_time")
    val endTime: String = "",
    @SerializedName("reward_unit")
    val rewardUnit: String = "",
    @SerializedName("reward")
    val reward: String = "",
    @SerializedName("eligible_tokens")
    val eligibleTokens: String = ""
)