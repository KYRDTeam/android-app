package com.kyberswap.android.presentation.main.walletconnect

import com.kyberswap.android.domain.model.WalletConnect

sealed class WalletConnectState {
    object Loading : WalletConnectState()
    class ShowError(val message: String?) : WalletConnectState()
    class Success(
        val walletConnect: WalletConnect
    ) : WalletConnectState()
}
