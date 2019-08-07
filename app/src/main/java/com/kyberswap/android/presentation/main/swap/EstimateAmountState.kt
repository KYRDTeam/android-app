package com.kyberswap.android.presentation.main.swap

sealed class EstimateAmountState {
    object Loading : EstimateAmountState()
    class ShowError(val message: String?) : EstimateAmountState()
    class Success(val amount: String) : EstimateAmountState()
}
