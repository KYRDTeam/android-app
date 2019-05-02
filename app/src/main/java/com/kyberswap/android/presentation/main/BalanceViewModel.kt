package com.kyberswap.android.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.splash.GetWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class BalanceViewModel @Inject constructor(
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase
) : ViewModel() {

    private val _getWalletCallback = MutableLiveData<Event<GetWalletState>>()
    val getWalletCallback: LiveData<Event<GetWalletState>>
        get() = _getWalletCallback

    fun getWallet(address: String) {
        getWalletByAddressUseCase.execute(
            Consumer {
                _getWalletCallback.value = Event(GetWalletState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getWalletCallback.value = Event(GetWalletState.ShowError(it.localizedMessage))
            },
            address
        )
    }

    override fun onCleared() {
        getBalancePollingUseCase.dispose()
        getWalletByAddressUseCase.dispose()
        super.onCleared()
    }

}