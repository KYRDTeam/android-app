package com.kyberswap.android.presentation.main.transaction

sealed class SaveTransactionFilterState {
    object Loading : SaveTransactionFilterState()
    class ShowError(val message: String?) : SaveTransactionFilterState()
    class Success(val status: String?) :
        SaveTransactionFilterState()
}
