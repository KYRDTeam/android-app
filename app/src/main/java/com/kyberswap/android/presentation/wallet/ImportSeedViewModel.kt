package com.kyberswap.android.presentation.wallet

import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromSeedUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Consumer
import org.consenlabs.tokencore.wallet.model.TokenException
import javax.inject.Inject

class ImportSeedViewModel @Inject constructor(
    private val importWalletFromSeedUseCase: ImportWalletFromSeedUseCase,
    getTokenBalanceUseCase: GetTokenBalanceUseCase
) : ImportWalletViewModel(getTokenBalanceUseCase) {


    fun importFromSeed(seed: String, walletName: String) {
        importWalletCallback.postValue(ImportWalletState.Loading)
        importWalletFromSeedUseCase.execute(
            Consumer {
                loadBalances(it)
            },
            Consumer {
                if (it is TokenException) {
                    importWalletCallback.value = ImportWalletState.ShowError(it.message)
                } else {
                    importWalletCallback.value = ImportWalletState.ShowError(it.localizedMessage)
                }
            },
            ImportWalletFromSeedUseCase.Param(seed, walletName)
        )
    }

    override fun onCleared() {
        super.onCleared()
        importWalletFromSeedUseCase.dispose()
    }
}