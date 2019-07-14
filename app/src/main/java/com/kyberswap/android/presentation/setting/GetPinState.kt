package com.kyberswap.android.presentation.setting

import com.kyberswap.android.domain.model.PassCode

sealed class GetPinState {
    object Loading : GetPinState()
    class ShowError(val message: String?) : GetPinState()
    class Success(val passCode: PassCode) : GetPinState()
}
