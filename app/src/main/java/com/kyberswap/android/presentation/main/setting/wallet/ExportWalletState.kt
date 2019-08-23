package com.kyberswap.android.presentation.main.setting.wallet

sealed class ExportWalletState {
    object Loading : ExportWalletState()
    class ShowError(val message: String?) : ExportWalletState()
    class Success(val value: String) : ExportWalletState()
}
