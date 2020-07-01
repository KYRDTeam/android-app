package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.PlatformFee

sealed class GetPlatformFeeState {
    object Loading : GetPlatformFeeState()
    class ShowError(val message: String?) : GetPlatformFeeState()
    class Success(val platformFee: PlatformFee) : GetPlatformFeeState()
}
