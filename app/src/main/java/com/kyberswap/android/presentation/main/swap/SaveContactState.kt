package com.kyberswap.android.presentation.main.swap

sealed class SaveContactState {
    object Loading : SaveContactState()
    class ShowError(val message: String?) : SaveContactState()
    class Success(val message: String? = null) : SaveContactState()
}
