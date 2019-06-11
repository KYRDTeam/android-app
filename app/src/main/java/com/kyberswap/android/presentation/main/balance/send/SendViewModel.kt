package com.kyberswap.android.presentation.main.balance.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.contact.GetContactUseCase
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateTransferGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT_SEND_ETH
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT_SEND_TOKEN
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SendViewModel @Inject constructor(
    private val getSendTokenUseCase: GetSendTokenUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val saveSendUseCase: SaveSendUseCase,
    private val getContactUseCase: GetContactUseCase,
    private val estimateTransferGasUseCase: EstimateTransferGasUseCase
) : ViewModel() {
    val compositeDisposable = CompositeDisposable()
    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback

    private val _getSendCallback = MutableLiveData<Event<GetSendState>>()
    val getSendCallback: LiveData<Event<GetSendState>>
        get() = _getSendCallback

    private val _getContactCallback = MutableLiveData<Event<GetContactState>>()
    val getContactCallback: LiveData<Event<GetContactState>>
        get() = _getContactCallback

    private val _saveSendCallback = MutableLiveData<Event<SaveSendState>>()
    val saveSendCallback: LiveData<Event<SaveSendState>>
        get() = _saveSendCallback

    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    fun getSendInfo(address: String) {
        getSendTokenUseCase.execute(
            Consumer {
                it.gasLimit =
                    if (it.tokenSource.isETH) DEFAULT_GAS_LIMIT_SEND_ETH.toString() else DEFAULT_GAS_LIMIT_SEND_TOKEN.toString()
                _getSendCallback.value = Event(GetSendState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getSendCallback.value = Event(GetSendState.ShowError(it.localizedMessage))
            },
            GetSendTokenUseCase.Param(address)
        )
    }


    fun getGasPrice() {
        getGasPriceUseCase.execute(
            Consumer {
                _getGetGasPriceCallback.value = Event(GetGasPriceState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getGetGasPriceCallback.value =
                    Event(GetGasPriceState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun saveSend(send: Send?, address: String = "") {
        send?.let {
            saveSendUseCase.execute(
                Action {
                    if (address.isNotEmpty()) {
                        _saveSendCallback.value = Event(SaveSendState.Success(""))
                    }
                },
                Consumer { error ->
                    error.printStackTrace()
                    _saveSendCallback.value = Event(SaveSendState.ShowError(error.localizedMessage))
                },
                SaveSendUseCase.Param(it, address)
            )
        }

    }

    fun getContact(walletAddress: String) {
        getContactUseCase.execute(
            Consumer {
                _getContactCallback.value = Event(GetContactState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getContactCallback.value = Event(GetContactState.ShowError(it.localizedMessage))
            },
            GetContactUseCase.Param(walletAddress)
        )
    }

    fun getGasLimit(send: Send?, wallet: Wallet?) {
        if (send == null || wallet == null) return
        estimateTransferGasUseCase.execute(
            Consumer {
                _getGetGasLimitCallback.value = Event(
                    GetGasLimitState.Success(
                        it.amountUsed.toBigDecimal()
                            .multiply(1.2.toBigDecimal()).toBigInteger()
                    )
                )
            },
            Consumer { },
            EstimateTransferGasUseCase.Param(wallet, send)
        )
    }
}