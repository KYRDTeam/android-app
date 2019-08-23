package com.kyberswap.android.presentation.landing

import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word

sealed class GetMnemonicState {
    object Loading : GetMnemonicState()
    class ShowError(val message: String?) : GetMnemonicState()
    class Success(val wallet: Wallet, val words: List<Word>) : GetMnemonicState()
}
