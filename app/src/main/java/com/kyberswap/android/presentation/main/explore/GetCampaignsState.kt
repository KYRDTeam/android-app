package com.kyberswap.android.presentation.main.explore

import com.kyberswap.android.domain.model.Campaign

sealed class GetCampaignsState {
    object Loading : GetCampaignsState()
    class ShowError(val message: String?) : GetCampaignsState()
    class Success(val campaigns: List<Campaign>) : GetCampaignsState()
}
