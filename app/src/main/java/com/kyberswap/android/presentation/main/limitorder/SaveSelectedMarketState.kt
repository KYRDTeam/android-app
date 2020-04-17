package com.kyberswap.android.presentation.main.limitorder

sealed class SaveSelectedMarketState {
    object Loading : SaveSelectedMarketState()
    class ShowError(val message: String?) :
        SaveSelectedMarketState()

    class Success(val message: String = "") : SaveSelectedMarketState()
}
