package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.Fee

sealed class GetFeeState {
    object Loading : GetFeeState()
    class ShowError(val message: String?, val isNetworkUnAvailable: Boolean = false) : GetFeeState()
    class Success(val fee: Fee) : GetFeeState()
}
