package com.kyberswap.android.presentation.main.profile.kyc

import com.kyberswap.android.domain.model.KycResponseStatus

sealed class ReSubmitState {
    object Loading : ReSubmitState()
    class ShowError(val message: String?) : ReSubmitState()
    class Success(val status: KycResponseStatus) : ReSubmitState()
}
