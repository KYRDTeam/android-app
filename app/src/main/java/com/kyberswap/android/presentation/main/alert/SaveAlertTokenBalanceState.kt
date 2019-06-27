package com.kyberswap.android.presentation.main.alert

sealed class SaveAlertTokenBalanceState {
    object Loading : SaveAlertTokenBalanceState()
    class ShowError(val message: String?) : SaveAlertTokenBalanceState()
    class Success(val status: String?) : SaveAlertTokenBalanceState()
}
