package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.swap.EstimateGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.domain.usecase.swap.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.common.specialGasLimitDefault
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SwapConfirmViewModel @Inject constructor(
    private val getSwapData: GetSwapDataUseCase,
    private val estimateGasUseCase: EstimateGasUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val swapTokenUseCase: SwapTokenUseCase
) : ViewModel() {

    private val _getSwapCallback = MutableLiveData<Event<GetSwapState>>()
    val getSwapDataCallback: LiveData<Event<GetSwapState>>
        get() = _getSwapCallback

    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    private val _swapTokenTransactionCallback =
        MutableLiveData<Event<SwapTokenTransactionState>>()
    val swapTokenTransactionCallback: LiveData<Event<SwapTokenTransactionState>>
        get() = _swapTokenTransactionCallback

    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback


    fun getSwapData(wallet: Wallet) {
        getSwapData.execute(
            Consumer {
                _getSwapCallback.value = Event(GetSwapState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getSwapCallback.value = Event(GetSwapState.ShowError(it.localizedMessage))
            },
            GetSwapDataUseCase.Param(wallet)
        )
    }

    fun swap(wallet: Wallet?, swap: Swap?) {
        swap?.let { sw ->
            _swapTokenTransactionCallback.postValue(Event(SwapTokenTransactionState.Loading))
            swapTokenUseCase.execute(
                Consumer {
                    _swapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.Success(it, sw))
                },
                Consumer {
                    it.printStackTrace()
                    _swapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.ShowError(it.localizedMessage, sw))
                },
                SwapTokenUseCase.Param(wallet!!, sw)

            )
        }
    }

    fun getGasPrice() {
        getGasPriceUseCase.dispose()
        getGasPriceUseCase.execute(
            Consumer {
                _getGetGasPriceCallback.value = Event(GetGasPriceState.Success(it))
            },
            Consumer {
                it.printStackTrace()
            },
            null
        )
    }

    fun getGasLimit(wallet: Wallet?, swap: Swap?) {
        if (wallet == null || swap == null) return
        estimateGasUseCase.dispose()
        estimateGasUseCase.execute(
            Consumer {
                val gasLimit = it.toBigInteger()

                val specialGasLimit = specialGasLimitDefault(swap.tokenSource, swap.tokenDest)

                _getGetGasLimitCallback.value = Event(
                    GetGasLimitState.Success(
                        if (specialGasLimit != null) {
                            specialGasLimit.max(gasLimit)
                        } else {
                            gasLimit
                        }
                    )
                )

            },
            Consumer {
                it.printStackTrace()
            },
            EstimateGasUseCase.Param(
                wallet,
                swap.tokenSource,
                swap.tokenDest,
                swap.sourceAmount,
                swap.minConversionRate
            )
        )
    }

    override fun onCleared() {
        getSwapData.dispose()
        estimateGasUseCase.dispose()
        getGasPriceUseCase.dispose()
        swapTokenUseCase.dispose()
        super.onCleared()
    }
}