package com.kyberswap.android.presentation.main.setting.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class BackupWalletInfoViewModel @Inject constructor(
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val saveWalletUseCase: UpdateWalletUseCase,
    errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _saveWalletCallback = MutableLiveData<Event<SaveWalletState>>()
    val saveWalletCallback: LiveData<Event<SaveWalletState>>
        get() = _saveWalletCallback

    fun save(wallet: Wallet) {
        saveWalletUseCase.dispose()
        saveWalletUseCase.execute(
            Action {
                _saveWalletCallback.value = Event(SaveWalletState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveWalletCallback.value = Event(SaveWalletState.ShowError(it.localizedMessage))
            },
            wallet
        )
    }

    override fun onCleared() {
        saveWalletUseCase.dispose()
        super.onCleared()
    }
}