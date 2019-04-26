package com.kyberswap.android.presentation.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetMnemonicUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LandingActivityViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase,
    private val getMnemonicUseCase: GetMnemonicUseCase
) : ViewModel() {

    private val _getMnemonicCallback = MutableLiveData<Event<GetMnemonicState>>()
    val getMnemonicCallback: LiveData<Event<GetMnemonicState>>
        get() = _getMnemonicCallback

    fun createWallet(pinLock: String = "") {
        _getMnemonicCallback.postValue(Event(GetMnemonicState.Loading))
        createWalletUseCase.execute(
            Consumer { wallet ->
                getMnemonicUseCase.execute(
                    Consumer {
                        _getMnemonicCallback.value =
                            Event(GetMnemonicState.Success(it, Wallet(wallet)))
                    },
                    Consumer {
                        _getMnemonicCallback.value =
                            Event(GetMnemonicState.ShowError(it.localizedMessage))
                    },
                    GetMnemonicUseCase.Param(pinLock, wallet.id)
                )
            },
            Consumer {
                it.printStackTrace()
                _getMnemonicCallback.value =
                    Event(GetMnemonicState.ShowError(it.localizedMessage))
            },
            CreateWalletUseCase.Param(pinLock)
        )
    }

    override fun onCleared() {
        createWalletUseCase.dispose()
        super.onCleared()
    }
}