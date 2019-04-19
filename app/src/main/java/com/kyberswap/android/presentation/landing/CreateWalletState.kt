package com.kyberswap.android.presentation.landing

import org.consenlabs.tokencore.wallet.Wallet

sealed class CreateWalletState {
    object Loading : CreateWalletState()
    class ShowError(val message: String?) : CreateWalletState()
    class Success(val wallet: Wallet) : CreateWalletState()
}
