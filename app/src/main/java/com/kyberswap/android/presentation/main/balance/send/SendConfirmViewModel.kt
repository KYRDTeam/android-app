package com.kyberswap.android.presentation.main.balance.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.GetSendState
import com.kyberswap.android.presentation.main.swap.TransferTokenTransactionState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SendConfirmViewModel @Inject constructor(
    private val getSendTokenUseCase: GetSendTokenUseCase,
    private val transferTokenUseCase: TransferTokenUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _getSendCallback = MutableLiveData<Event<GetSendState>>()
    val getSendCallback: LiveData<Event<GetSendState>>
        get() = _getSendCallback


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

    fun send(wallet: Wallet?, send: Send?) {
        send?.let {
            _transferTokenTransactionCallback.postValue(Event(TransferTokenTransactionState.Loading))
            transferTokenUseCase.execute(
                Consumer {
                    _transferTokenTransactionCallback.value =
                        Event(TransferTokenTransactionState.Success(it))
                },
                Consumer {
                    it.printStackTrace()
                    _transferTokenTransactionCallback.value =
                        Event(TransferTokenTransactionState.ShowError(errorHandler.getError(it)))
                },
                TransferTokenUseCase.Param(wallet!!, send)

            )
        }
    }

}