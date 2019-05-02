package com.kyberswap.android.presentation.main.balance

sealed class GetUnitState {
    object Loading : GetUnitState()
    class ShowError(val message: String?) : GetUnitState()
    class Success(val unit: String) : GetUnitState()
}
