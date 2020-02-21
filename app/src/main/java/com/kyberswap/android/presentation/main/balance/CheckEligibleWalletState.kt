package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.EligibleWalletStatus
import com.kyberswap.android.domain.model.Swap

sealed class CheckEligibleWalletState {
    object Loading : CheckEligibleWalletState()
    class ShowError(val message: String?, val swap: Swap? = null) : CheckEligibleWalletState()
    class Success(val eligibleWalletStatus: EligibleWalletStatus, val swap: Swap? = null) :
        CheckEligibleWalletState()
}
