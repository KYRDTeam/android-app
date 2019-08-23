package com.kyberswap.android.presentation.main.balance

sealed class SaveWalletState {
    object Loading : SaveWalletState()
    class ShowError(val message: String?) : SaveWalletState()
    class Success(val status: String?) : SaveWalletState()
}
