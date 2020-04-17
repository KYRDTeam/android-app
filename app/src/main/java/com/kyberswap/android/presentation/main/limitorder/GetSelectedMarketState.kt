package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.MarketItem

sealed class GetSelectedMarketState {
    object Loading : GetSelectedMarketState()
    class ShowError(val message: String?) : GetSelectedMarketState()
    class Success(val market: MarketItem) : GetSelectedMarketState()
}
