package com.kyberswap.android.presentation.main.limitorder

sealed class SaveFilterState {
    object Loading : SaveFilterState()
    class ShowError(val message: String?) : SaveFilterState()
    class Success(val status: String?) : SaveFilterState()
}
