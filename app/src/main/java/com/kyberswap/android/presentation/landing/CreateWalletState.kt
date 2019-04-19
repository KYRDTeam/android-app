package com.kyberswap.android.presentation.landing

import com.kyberswap.android.domain.model.Word

sealed class CreateWalletState {
    object Loading : CreateWalletState()
    class ShowError(val message: String?) : CreateWalletState()
    class Success(val words: List<Word>) : CreateWalletState()
}
