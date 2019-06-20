package com.kyberswap.android.presentation.splash

import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet

sealed class GetUserWalletState {
    object Loading : GetUserWalletState()
    class ShowError(val message: String?) : GetUserWalletState()
    class Success(val wallet: Wallet, val userInfo: UserInfo?) : GetUserWalletState()
}
