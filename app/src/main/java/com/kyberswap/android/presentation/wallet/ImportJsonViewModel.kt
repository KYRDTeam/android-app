package com.kyberswap.android.presentation.wallet

import android.net.Uri
import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromJsonUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ImportJsonViewModel @Inject constructor(
    private val importWalletFromJsonUseCase: ImportWalletFromJsonUseCase,
    getTokenBalance: GetTokenBalanceUseCase
) : ImportWalletViewModel(getTokenBalance) {

    fun importFromJson(uri: Uri, password: String, walletName: String) {
        importWalletCallback.postValue(ImportWalletState.Loading)
        importWalletFromJsonUseCase.execute(
            Consumer {
                loadBalances(it)
            },
            Consumer {
                it.printStackTrace()
                importWalletCallback.value = ImportWalletState.ShowError(it.localizedMessage)
            },
            ImportWalletFromJsonUseCase.Param(uri, password, walletName)
        )
    }

    override fun onCleared() {
        super.onCleared()
        importWalletFromJsonUseCase.dispose()
    }
}