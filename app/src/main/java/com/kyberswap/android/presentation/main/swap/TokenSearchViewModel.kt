package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetTokenTransferUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class TokenSearchViewModel @Inject constructor(
    private val getTokenListUseCase: GetTokenTransferUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val saveSwapDataTokenUseCase: SaveSwapDataTokenUseCase,
    private val saveSendTokenUseCase: SaveSendTokenUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _getTokenListCallback = MutableLiveData<Event<GetBalanceState>>()
    val getTokenListCallback: LiveData<Event<GetBalanceState>>
        get() = _getTokenListCallback

    private val _saveSwapCallback = MutableLiveData<Event<SaveSwapDataState>>()
    val saveSwapCallback: LiveData<Event<SaveSwapDataState>>
        get() = _saveSwapCallback

    private val _saveSendCallback = MutableLiveData<Event<SaveSendState>>()
    val saveSendCallback: LiveData<Event<SaveSendState>>
        get() = _saveSendCallback

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun getTokenList(address: String, isSend: Boolean = false) {
        getTokenListUseCase.execute(
            Consumer {
                _getTokenListCallback.value = Event(
                    GetBalanceState.Success(
                        it.filter { if (!isSend) it.isListed && !it.isOther else isSend }
                            .sortedByDescending { it.currentBalance }.sortedByDescending { it.currentBalance * it.rateEthNow }
                    )
                )
            },
            Consumer {
                it.printStackTrace()
                _getTokenListCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            errorHandler.getError(it)
                        )
                    )
            },
            address
        )
    }

    override fun onCleared() {
        getWalletByAddressUseCase.dispose()
        saveSwapDataTokenUseCase.dispose()
        getTokenListUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun saveTokenSelection(walletAddress: String, token: Token, sourceToken: Boolean) {
        saveSwapDataTokenUseCase.execute(
            Action {
                _saveSwapCallback.value = Event(SaveSwapDataState.Success())
            },
            Consumer {
                it.printStackTrace()
                _saveSwapCallback.value =
                    Event(SaveSwapDataState.ShowError(errorHandler.getError(it)))
            },
            SaveSwapDataTokenUseCase.Param(walletAddress, token, sourceToken)
        )
    }

    fun saveSendTokenSelection(address: String, token: Token) {
        saveSendTokenUseCase.execute(
            Action {
                _saveSendCallback.value = Event(SaveSendState.Success())
            },
            Consumer {
                it.printStackTrace()
                _saveSendCallback.value =
                    Event(SaveSendState.ShowError(errorHandler.getError(it)))
            },
            SaveSendTokenUseCase.Param(address, token)
        )
    }
}