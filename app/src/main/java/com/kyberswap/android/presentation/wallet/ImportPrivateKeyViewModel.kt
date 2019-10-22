package com.kyberswap.android.presentation.wallet

import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromPrivateKeyUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Consumer
import org.consenlabs.tokencore.wallet.model.TokenException
import javax.inject.Inject

class ImportPrivateKeyViewModel @Inject constructor(
    private val importWalletFromPrivateKeyUseCase: ImportWalletFromPrivateKeyUseCase,
    getTokenBalance: GetTokensBalanceUseCase
) : ImportWalletViewModel(getTokenBalance) {


    fun importFromPrivateKey(privateKey: String, walletName: String) {
        importWalletCallback.postValue(ImportWalletState.Loading)
        importWalletFromPrivateKeyUseCase.execute(
            Consumer {
                loadBalances(it)
            },
            Consumer {
                it.printStackTrace()
                if (it is TokenException) {
                    importWalletCallback.value = ImportWalletState.ShowError(it.message)
                } else {
                    importWalletCallback.value = ImportWalletState.ShowError(it.localizedMessage)
                }
            },
            ImportWalletFromPrivateKeyUseCase.Param(privateKey, walletName)
        )
    }
}