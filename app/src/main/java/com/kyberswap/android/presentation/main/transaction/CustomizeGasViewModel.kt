package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.domain.usecase.transaction.SpeedUpOrCancelTransactionUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class CustomizeGasViewModel @Inject constructor(
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val speedUpOrCancelOrCancelTransactionUseCase: SpeedUpOrCancelTransactionUseCase,
    getWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getWalletUseCase, errorHandler) {

    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback

    private val _speedUpTransactionCallback = MutableLiveData<Event<SpeedUpTransactionState>>()
    val speedUpTransactionCallback: LiveData<Event<SpeedUpTransactionState>>
        get() = _speedUpTransactionCallback


    fun getGasPrice() {
        getGasPriceUseCase.dispose()
        getGasPriceUseCase.execute(
            Consumer {
                _getGetGasPriceCallback.value = Event(GetGasPriceState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getGetGasPriceCallback.value =
                    Event(GetGasPriceState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun speedUp(wallet: Wallet, transaction: Transaction) {
        speedUpOrCancelOrCancelTransactionUseCase.dispose()
        _speedUpTransactionCallback.postValue(Event(SpeedUpTransactionState.Loading))
        speedUpOrCancelOrCancelTransactionUseCase.execute(
            Consumer {
                _speedUpTransactionCallback.value = Event(SpeedUpTransactionState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _speedUpTransactionCallback.value =
                    Event(SpeedUpTransactionState.ShowError(it.localizedMessage))
            },
            SpeedUpOrCancelTransactionUseCase.Param(transaction, wallet)
        )
    }
}