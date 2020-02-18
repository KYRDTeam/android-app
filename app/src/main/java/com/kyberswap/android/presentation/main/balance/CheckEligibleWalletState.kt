package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.EligibleWalletStatus

sealed class CheckEligibleWalletState {
    object Loading : CheckEligibleWalletState()
    class ShowError(val message: String?) : CheckEligibleWalletState()
    class Success(val eligibleWalletStatus: EligibleWalletStatus) : CheckEligibleWalletState()
}
