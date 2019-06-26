package com.kyberswap.android.presentation.main.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.alert.SaveAlertTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetTokenUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PriceAlertTokenSearchViewModel @Inject constructor(
    private val getTokenListUseCase: GetTokenUseCase,
    private val saveAlertTokenUseCase: SaveAlertTokenUseCase

) : ViewModel() {

    private val _getTokenListCallback = MutableLiveData<Event<GetBalanceState>>()
    val getTokenListCallback: LiveData<Event<GetBalanceState>>
        get() = _getTokenListCallback

    private val _saveAlertTokenState = MutableLiveData<Event<SaveAlertTokenBalanceState>>()
    val saveAlertTokenState: LiveData<Event<SaveAlertTokenBalanceState>>
        get() = _saveAlertTokenState


    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun getTokenList(address: String) {
        getTokenListUseCase.execute(
            Consumer {
                _getTokenListCallback.value = Event(
                    GetBalanceState.Success(
                        it
                    )
                )
    ,
            Consumer {
                it.printStackTrace()
                _getTokenListCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            it.localizedMessage
                        )
                    )
    ,
            address
        )
    }

    override fun onCleared() {
        getTokenListUseCase.dispose()
        super.onCleared()
    }

    fun saveToken(wallet: Wallet?, token: Token) {
        if (wallet == null) return
        saveAlertTokenUseCase.execute(
            Action {
                _saveAlertTokenState.value = Event(SaveAlertTokenBalanceState.Success(""))
    ,
            Consumer {
                it.printStackTrace()
                _saveAlertTokenState.value =
                    Event(SaveAlertTokenBalanceState.ShowError(it.localizedMessage))
    ,
            SaveAlertTokenUseCase.Param(wallet.address, token)
        )
    }

}