package com.kyberswap.android.presentation.setting

import com.kyberswap.android.domain.model.VerifyStatus

sealed class VerifyPinState {
    object Loading : VerifyPinState()
    class ShowError(val message: String?) : VerifyPinState()
    class Success(val verifyStatus: VerifyStatus) : VerifyPinState()
}
