package com.kyberswap.android.presentation.main.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import com.kyberswap.android.domain.usecase.token.SaveTokenUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class BalanceViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val saveSwapDataTokenUseCase: SaveSwapDataTokenUseCase,
    private val saveSendTokenUseCase: SaveSendTokenUseCase,
    private val prepareBalanceUseCase: PrepareBalanceUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _refreshBalanceStateCallback = MutableLiveData<Event<GetBalanceState>>()
    val refreshBalanceStateCallback: LiveData<Event<GetBalanceState>>
        get() = _refreshBalanceStateCallback

    private val _getBalanceStateCallback = MutableLiveData<Event<GetBalanceState>>()
    val getBalanceStateCallback: LiveData<Event<GetBalanceState>>
        get() = _getBalanceStateCallback

    private val _saveTokenCallback = MutableLiveData<Event<SaveTokenState>>()
    val saveTokenCallback: LiveData<Event<SaveTokenState>>
        get() = _saveTokenCallback

    val visibilityCallback: LiveData<Event<Boolean>>
        get() = _visibility

    private val _visibility = MutableLiveData<Event<Boolean>>()

    private val _saveWalletCallback = MutableLiveData<Event<SaveWalletState>>()
    val saveWalletCallback: LiveData<Event<SaveWalletState>>
        get() = _saveWalletCallback

    private val _callback = MutableLiveData<Event<SaveSwapDataState>>()
    val callback: LiveData<Event<SaveSwapDataState>>
        get() = _callback

    private val _callbackSaveSend = MutableLiveData<Event<SaveSendState>>()
    val callbackSaveSend: LiveData<Event<SaveSendState>>
        get() = _callbackSaveSend

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun updateVisibility(isVisible: Boolean) {
        _visibility.value = Event(isVisible)
    }

    fun getTokenBalance() {
        getBalanceUseCase.dispose()
        getBalanceUseCase.execute(
            Consumer {
                _getBalanceStateCallback.value = Event(GetBalanceState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getBalanceStateCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            errorHandler.getError(it)
                        )
                    )
            },
            null
        )
    }

    fun updateWallet(wallet: Wallet?) {
        if (wallet == null) return
        updateWalletUseCase.execute(
            Action {
                _saveWalletCallback.value = Event(SaveWalletState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveWalletCallback.value =
                    Event(SaveWalletState.ShowError(errorHandler.getError(it)))
            },
            wallet
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        getWalletByAddressUseCase.dispose()
        getBalanceUseCase.dispose()
        updateWalletUseCase.dispose()
        saveSendTokenUseCase.dispose()
        saveSwapDataTokenUseCase.dispose()
        prepareBalanceUseCase.dispose()
        saveTokenUseCase.dispose()
        super.onCleared()
    }


    fun save(walletAddress: String, token: Token, isSell: Boolean = false) {
        saveSwapDataTokenUseCase.execute(
            Action {
                _callback.value = Event(SaveSwapDataState.Success())
            },
            Consumer {
                it.printStackTrace()
                _callback.value =
                    Event(SaveSwapDataState.ShowError(errorHandler.getError(it)))
            },
            SaveSwapDataTokenUseCase.Param(walletAddress, token, isSell)
        )
    }

    fun saveSendToken(address: String, token: Token) {
        saveSendTokenUseCase.execute(
            Action {
                _callbackSaveSend.value = Event(SaveSendState.Success())
            },
            Consumer {
                _callbackSaveSend.value = Event(SaveSendState.Success())
            },
            SaveSendTokenUseCase.Param(address, token)
        )
    }

    fun refresh() {
        var count = 0
        prepareBalanceUseCase.execute(
            Consumer {
                Timber.e("refresh")
                count++
                _refreshBalanceStateCallback.value = Event(
                    GetBalanceState.Success(
                        it,
                        isCompleted = count == 2 // both local and remote resource return
                    )
                )
            },
            Consumer { error ->
                error.printStackTrace()
                _refreshBalanceStateCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            errorHandler.getError(error)
                        )
                    )
            },
            PrepareBalanceUseCase.Param()
        )
    }

    fun saveFav(token: Token) {
        getBalanceUseCase.dispose()
        saveTokenUseCase.execute(
            Action {
                getTokenBalance()
                _saveTokenCallback.value = Event(SaveTokenState.Success(token.fav))
            },
            Consumer {
                getTokenBalance()
                it.printStackTrace()
                _saveTokenCallback.value =
                    Event(SaveTokenState.ShowError(errorHandler.getError(it)))
            },
            SaveTokenUseCase.Param(token)
        )
    }
}