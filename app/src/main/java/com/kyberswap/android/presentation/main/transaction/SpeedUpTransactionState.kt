package com.kyberswap.android.presentation.main.transaction

sealed class SpeedUpTransactionState {
    object Loading : SpeedUpTransactionState()
    class ShowError(val message: String?) : SpeedUpTransactionState()
    class Success(val status: Boolean) : SpeedUpTransactionState()
}
