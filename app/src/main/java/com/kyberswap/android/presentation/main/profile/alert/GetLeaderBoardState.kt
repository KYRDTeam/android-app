package com.kyberswap.android.presentation.main.profile.alert

import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.CampaignInfo

sealed class GetLeaderBoardState {
    object Loading : GetLeaderBoardState()
    class ShowError(val message: String?) : GetLeaderBoardState()
    class Success(
        val alerts: List<Alert>,
        val campaignInfo: CampaignInfo,
        val lastCampaignTitle: String
    ) : GetLeaderBoardState()
}
