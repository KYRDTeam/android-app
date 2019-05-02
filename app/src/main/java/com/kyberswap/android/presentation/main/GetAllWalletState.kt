package com.kyberswap.android.presentation.main

import com.kyberswap.android.domain.model.Wallet

sealed class GetAllWalletState {
    object Loading : GetAllWalletState()
    class ShowError(val message: String?) : GetAllWalletState()
    class Success(val wallets: List<Wallet>) : GetAllWalletState()
}
