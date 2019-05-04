package com.kyberswap.android.presentation.main.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.splash.GetWalletState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class KyberListViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase
) : ViewModel() {

    private val _getBalanceStateCallback = MutableLiveData<Event<GetBalanceState>>()
    val getBalanceStateCallback: LiveData<Event<GetBalanceState>>
        get() = _getBalanceStateCallback

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

    fun getTokenBalance(address: String) {
        getBalancePollingUseCase.execute(
            Consumer {


            },
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
            },
            GetBalancePollingUseCase.Param(address)
        )
        _getBalanceStateCallback.postValue(Event(GetBalanceState.Loading))
        getBalanceUseCase.execute(
            Consumer {

                _getBalanceStateCallback.value = Event(
                    GetBalanceState.Success(
                        it
                    )
                )
            },
            Consumer {
                it.printStackTrace()
                _getBalanceStateCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            it.localizedMessage
                        )
                    )
            },
            null
        )


    }

    fun updateWallet(wallet: Wallet) {
        updateWalletUseCase.execute(
            Action {

            },
            Consumer {
                it.printStackTrace()
            },
            wallet
        )
    }
}