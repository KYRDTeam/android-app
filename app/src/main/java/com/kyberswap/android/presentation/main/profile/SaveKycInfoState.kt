package com.kyberswap.android.presentation.main.profile

sealed class SaveKycInfoState {
    object Loading : SaveKycInfoState()
    class ShowError(val message: String?) : SaveKycInfoState()
    class Success(val status: String?) : SaveKycInfoState()
}
