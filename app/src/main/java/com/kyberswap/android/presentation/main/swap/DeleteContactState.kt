package com.kyberswap.android.presentation.main.swap

sealed class DeleteContactState {
    object Loading : DeleteContactState()
    class ShowError(val message: String?) : DeleteContactState()
    class Success(val message: String? = null) : DeleteContactState()
}
