package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.RatingInfo

sealed class GetRatingInfoState {
    object Loading : GetRatingInfoState()
    class ShowError(val message: String?) : GetRatingInfoState()
    class Success(val ratingInfo: RatingInfo) : GetRatingInfoState()
}
