package com.kyberswap.android.presentation.main.kybercode

import com.kyberswap.android.domain.model.Wallet

sealed class KyberCodeState {
    object Loading : KyberCodeState()
    class ShowError(val message: String?) : KyberCodeState()
    class Success(val wallet: Wallet) : KyberCodeState()
}
