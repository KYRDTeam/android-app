package com.kyberswap.android.presentation.main.balance.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateTransferGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.presentation.common.ADDITIONAL_SEND_GAS_LIMIT
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.common.calculateDefaultGasLimitTransfer
import com.kyberswap.android.presentation.common.specialGasLimitDefault
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.TransferTokenTransactionState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import java.math.BigInteger
import javax.inject.Inject

class SendConfirmViewModel @Inject constructor(
    private val getSendTokenUseCase: GetSendTokenUseCase,
    private val transferTokenUseCase: TransferTokenUseCase,
    private val estimateTransferGasUseCase: EstimateTransferGasUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _getSendCallback = MutableLiveData<Event<GetSendState>>()
    val getSendCallback: LiveData<Event<GetSendState>>
        get() = _getSendCallback

    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback

    private val _transferTokenTransactionCallback =
        MutableLiveData<Event<TransferTokenTransactionState>>()
    val transferTokenTransactionCallback: LiveData<Event<TransferTokenTransactionState>>
        get() = _transferTokenTransactionCallback

    fun getSendData(wallet: Wallet) {
        getSendTokenUseCase.execute(
            Consumer {
                _getSendCallback.value = Event(GetSendState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getSendCallback.value = Event(GetSendState.ShowError(errorHandler.getError(it)))
            },
            GetSendTokenUseCase.Param(wallet)
        )
    }

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

    fun getGasLimit(send: Send?, wallet: Wallet?) {
        if (send == null || wallet == null) return
        estimateTransferGasUseCase.dispose()
        estimateTransferGasUseCase.execute(
            Consumer {

                val gasLimit = calculateDefaultGasLimitTransfer(send.tokenSource)
                    .min(
                        it.amountUsed.multiply(120.toBigInteger())
                            .divide(100.toBigInteger()) + if (send.tokenSource.isETH) BigInteger.ZERO else ADDITIONAL_SEND_GAS_LIMIT.toBigInteger()
                    )

                val specialGasLimit = specialGasLimitDefault(send.tokenSource, send.tokenSource)

                _getGetGasLimitCallback.value = Event(
                    GetGasLimitState.Success(
                        if (specialGasLimit != null) {
                            specialGasLimit.min(gasLimit)
                        } else {
                            gasLimit
                        }
                    )
                )
            },
            Consumer {
                it.printStackTrace()
            },
            EstimateTransferGasUseCase.Param(wallet, send)
        )
    }

    fun send(wallet: Wallet?, send: Send?) {
        if (wallet == null) return
        if (send == null) return
        send.let {
            _transferTokenTransactionCallback.postValue(Event(TransferTokenTransactionState.Loading))
            transferTokenUseCase.execute(
                Consumer {
                    _transferTokenTransactionCallback.value =
                        Event(TransferTokenTransactionState.Success(it))
                },
                Consumer {
                    it.printStackTrace()
                    _transferTokenTransactionCallback.value =
                        Event(TransferTokenTransactionState.ShowError(it.localizedMessage))
                },
                TransferTokenUseCase.Param(wallet, send)

            )
        }
    }

    override fun onCleared() {
        getSendTokenUseCase.dispose()
        transferTokenUseCase.dispose()
        estimateTransferGasUseCase.dispose()
        getGasPriceUseCase.dispose()
        super.onCleared()
    }
}