package com.kyberswap.android.presentation.main.setting.wallet

import com.kyberswap.android.domain.model.VerifyStatus

sealed class DeleteWalletState {
    object Loading : DeleteWalletState()
    class ShowError(val message: String?) : DeleteWalletState()
    class Success(val verifyStatus: VerifyStatus) : DeleteWalletState()
}
