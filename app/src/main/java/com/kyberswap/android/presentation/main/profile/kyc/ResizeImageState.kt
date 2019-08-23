package com.kyberswap.android.presentation.main.profile.kyc

sealed class ResizeImageState {
    object Loading : ResizeImageState()
    class ShowError(val message: String?) : ResizeImageState()
    class Success(val stringImage: String) : ResizeImageState()
}
