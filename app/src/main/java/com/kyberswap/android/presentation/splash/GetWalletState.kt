package com.kyberswap.android.presentation.splash

import com.kyberswap.android.domain.model.Wallet

sealed class GetWalletState {
    object Loading : GetWalletState()
    class ShowError(val message: String?) : GetWalletState()
    class Success(val wallet: Wallet) : GetWalletState()
}
