package com.kyberswap.android.presentation.wallet

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromPrivateKeyUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ImportPrivateKeyViewModel @Inject constructor(
    private val importWalletFromPrivateKeyUseCase: ImportWalletFromPrivateKeyUseCase
) : ViewModel() {
    val importWalletCallback: MutableLiveData<ImportWalletState> = MutableLiveData()

    fun importFromPrivateKey(privateKey: String, walletName: String) {
        importWalletCallback.postValue(ImportWalletState.Loading)
        importWalletFromPrivateKeyUseCase.execute(
            Consumer {
                importWalletCallback.value = ImportWalletState.Success(it)
    ,
            Consumer {
                it.printStackTrace()
                importWalletCallback.value = ImportWalletState.ShowError(it.localizedMessage)
    ,
            ImportWalletFromPrivateKeyUseCase.Param(privateKey, walletName)
        )
    }

}