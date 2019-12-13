package com.kyberswap.android.presentation.main.swap

sealed class GetENSAddressState {
    object Loading : GetENSAddressState()
    class ShowError(val message: String?) : GetENSAddressState()
    class Success(val ensName: String, val address: String, val isFromContinue: Boolean = false) :
        GetENSAddressState()
}
