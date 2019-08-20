package com.kyberswap.android.presentation.main.transaction

sealed class ShowRefreshState {
    object Loading : ShowRefreshState()
    class ShowError(val message: String?) : ShowRefreshState()
    class Success(
        val isLoaded: Boolean = false
    ) : ShowRefreshState()
}
