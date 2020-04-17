package com.kyberswap.android.presentation.main.limitorder

sealed class SaveFavMarketState {
    object Loading : SaveFavMarketState()
    class ShowError(val message: String?) :
        SaveFavMarketState()

    class Success(val fav: Boolean, val isLogin: Boolean, val pair: String) : SaveFavMarketState()
}
