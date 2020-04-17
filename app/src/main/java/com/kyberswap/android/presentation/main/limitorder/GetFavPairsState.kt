package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.data.api.limitorder.FavoritePair

sealed class GetFavPairsState {
    object Loading : GetFavPairsState()
    class ShowError(val message: String?) :
        GetFavPairsState()

    class Success(val favoritePairs: List<FavoritePair>) : GetFavPairsState()
}
