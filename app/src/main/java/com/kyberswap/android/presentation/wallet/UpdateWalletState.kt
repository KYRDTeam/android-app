package com.kyberswap.android.presentation.wallet

import com.kyberswap.android.domain.model.Wallet

sealed class UpdateWalletState {
    object Loading : UpdateWalletState()
    class ShowError(val message: String?) : UpdateWalletState()
    class Success(val wallet: Wallet) : UpdateWalletState()
}
