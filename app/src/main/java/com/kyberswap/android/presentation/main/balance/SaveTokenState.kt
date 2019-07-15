package com.kyberswap.android.presentation.main.balance

sealed class SaveTokenState {
    object Loading : SaveTokenState()
    class ShowError(val message: String?) : SaveTokenState()
    class Success(val fav: Boolean) : SaveTokenState()
}
