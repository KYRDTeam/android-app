package com.kyberswap.android.presentation.main.swap

import java.math.BigInteger

sealed class GetGasLimitState {
    object Loading : GetGasLimitState()
    class ShowError(val message: String?) : GetGasLimitState()
    class Success(val gasLimit: BigInteger) : GetGasLimitState()
}
