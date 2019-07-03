package com.kyberswap.android.presentation.main.profile.kyc

import com.kyberswap.android.domain.model.KycResponseStatus

sealed class SavePersonalInfoState {
    object Loading : SavePersonalInfoState()
    class ShowError(val message: String?) : SavePersonalInfoState()
    class Success(val status: KycResponseStatus) : SavePersonalInfoState()
}
