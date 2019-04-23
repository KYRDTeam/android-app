package com.kyberswap.android.presentation.landing

import org.consenlabs.tokencore.wallet.Wallet


sealed class ImportWalletState {
    object Loading : ImportWalletState()
    class ShowError(val message: String?) : ImportWalletState()
    class Success(val wallet: Wallet) : ImportWalletState()
}
