package com.kyberswap.android.presentation.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromSeedUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ImportSeedViewModel @Inject constructor(
    private val importWalletFromSeedUseCase: ImportWalletFromSeedUseCase
) : ViewModel() {

    val importWalletCallback: MutableLiveData<ImportWalletState> = MutableLiveData()

    fun importFromSeed(seed: String, walletName: String) {
        importWalletCallback.postValue(ImportWalletState.Loading)
        importWalletFromSeedUseCase.execute(
            Consumer {
                importWalletCallback.value = ImportWalletState.Success(it)
            },
            Consumer {
                it.printStackTrace()
                importWalletCallback.value = ImportWalletState.ShowError(it.localizedMessage)
            },
            ImportWalletFromSeedUseCase.Param(seed, walletName)
        )
    }
}