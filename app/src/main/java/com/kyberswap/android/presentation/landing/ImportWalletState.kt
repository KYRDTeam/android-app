package com.kyberswap.android.presentation.landing

import com.kyberswap.android.domain.model.Wallet


sealed class ImportWalletState {
    object Loading : ImportWalletState()
    class ShowError(val message: String?) : ImportWalletState()
    class Success(val wallet: Wallet) : ImportWalletState()
}
