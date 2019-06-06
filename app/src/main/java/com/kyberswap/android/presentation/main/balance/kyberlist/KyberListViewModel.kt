package com.kyberswap.android.presentation.main.balance.kyberlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import com.kyberswap.android.presentation.main.swap.SaveSendState
import com.kyberswap.android.presentation.main.swap.SaveSwapDataState
import com.kyberswap.android.presentation.splash.GetWalletState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class KyberListViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val saveSwapDataTokenUseCase: SaveSwapDataTokenUseCase,
    private val saveSendTokenUseCase: SaveSendTokenUseCase,
    private val prepareBalanceUseCase: PrepareBalanceUseCase
) : ViewModel() {

    private val _getBalanceStateCallback = MutableLiveData<Event<GetBalanceState>>()
    val getBalanceStateCallback: LiveData<Event<GetBalanceState>>
        get() = _getBalanceStateCallback

    private val _getWalletCallback = MutableLiveData<Event<GetWalletState>>()
    val getWalletCallback: LiveData<Event<GetWalletState>>
        get() = _getWalletCallback

    val searchedKeywordsCallback: LiveData<Event<String>>
        get() = _searchedKeywords

    private val _searchedKeywords = MutableLiveData<Event<String>>()

    private val _saveSwapDataStateStateCallback = MutableLiveData<Event<SaveSwapDataState>>()
    val saveTokenSelectionCallback: LiveData<Event<SaveSwapDataState>>
        get() = _saveSwapDataStateStateCallback


    private val _callback = MutableLiveData<Event<SaveSwapDataState>>()
    val callback: LiveData<Event<SaveSwapDataState>>
        get() = _callback


    private val _callbackSaveSend = MutableLiveData<Event<SaveSendState>>()
    val callbackSaveSend: LiveData<Event<SaveSendState>>
        get() = _callbackSaveSend

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun updateSearchKeyword(keyword: String) {
        _searchedKeywords.value = Event(keyword)
    }

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
                _saveSwapDataStateStateCallback.value = Event(SaveSwapDataState.Success())
            },
            Consumer {
                it.printStackTrace()
                _saveSwapDataStateStateCallback.value =
                    Event(SaveSwapDataState.ShowError(it.localizedMessage))
            },
            wallet
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        getBalancePollingUseCase.dispose()
        getWalletByAddressUseCase.dispose()
        getBalanceUseCase.dispose()
        updateWalletUseCase.dispose()
        saveSendTokenUseCase.dispose()
        saveSwapDataTokenUseCase.dispose()
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
                    Event(SaveSwapDataState.ShowError(it.localizedMessage))
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
        prepareBalanceUseCase.execute(
            Consumer {
                _getBalanceStateCallback.value = Event(
                    GetBalanceState.Success(
                        it
                    )
                )
            },
            Consumer { error ->
                error.printStackTrace()
                _getBalanceStateCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            error.localizedMessage
                        )
                    )

            },
            PrepareBalanceUseCase.Param(true)
        )
    }
}