package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.MarketItem

sealed class GetMarketsState {
    object Loading : GetMarketsState()
    class ShowError(val message: String?) :
        GetMarketsState()

    class Success(val items: List<MarketItem>) : GetMarketsState()
}
