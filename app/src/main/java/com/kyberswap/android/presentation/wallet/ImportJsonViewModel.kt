package com.kyberswap.android.presentation.wallet

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.ImportWalletFromJsonUseCase
import com.kyberswap.android.presentation.landing.ImportWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ImportJsonViewModel @Inject constructor(
    private val importWalletFromJsonUseCase: ImportWalletFromJsonUseCase
) : ViewModel() {
    val importWalletCallback: MutableLiveData<ImportWalletState> = MutableLiveData()

    fun importFromJson(uri: Uri, password: String, walletName: String) {
        importWalletCallback.postValue(ImportWalletState.Loading)
        importWalletFromJsonUseCase.execute(
            Consumer {
                importWalletCallback.value = ImportWalletState.Success(it)
    ,
            Consumer {
                it.printStackTrace()
                importWalletCallback.value = ImportWalletState.ShowError(it.localizedMessage)
    ,
            ImportWalletFromJsonUseCase.Param(uri, password, walletName)
        )
    }
}