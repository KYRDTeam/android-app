package com.kyberswap.android.presentation.main.swap

sealed class GetHintState {
    object Loading : GetHintState()
    class ShowError(val message: String?) : GetHintState()
    class Success(val hasReserveRouting: Boolean) : GetHintState()
}
