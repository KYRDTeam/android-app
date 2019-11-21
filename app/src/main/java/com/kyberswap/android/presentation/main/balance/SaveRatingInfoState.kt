package com.kyberswap.android.presentation.main.balance

sealed class SaveRatingInfoState {
    object Loading : SaveRatingInfoState()
    class ShowError(val message: String?) : SaveRatingInfoState()
    class Success(val status: String?) : SaveRatingInfoState()
}
