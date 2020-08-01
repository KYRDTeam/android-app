package com.kyberswap.android.presentation.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.SaveWalletState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class VerifyBackupWordViewModel @Inject constructor(
    private val updateWalletUseCase: UpdateWalletUseCase
) : ViewModel() {

    private val _saveWalletCallback = MutableLiveData<Event<SaveWalletState>>()
    val saveWalletCallback: LiveData<Event<SaveWalletState>>
        get() = _saveWalletCallback


    fun saveWallet(wallet: Wallet) {
        updateWalletUseCase.dispose()
        updateWalletUseCase.execute(
            Action {
                _saveWalletCallback.value = Event(SaveWalletState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveWalletCallback.value =
                    Event(SaveWalletState.ShowError(it.localizedMessage))
            },
            wallet
        )
    }

    override fun onCleared() {
        updateWalletUseCase.dispose()
        super.onCleared()
    }
}