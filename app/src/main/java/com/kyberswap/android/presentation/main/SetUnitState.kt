package com.kyberswap.android.presentation.main

sealed class SetUnitState {
    object Loading : SetUnitState()
    class ShowError(val message: String?) : SetUnitState()
    class Success(val unit: String) : SetUnitState()
}
