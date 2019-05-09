package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapDataTokenUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class TokenSearchViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val saveSwapDataTokenUseCase: SaveSwapDataTokenUseCase
) : ViewModel() {

    private val _getBalanceStateCallback = MutableLiveData<Event<GetBalanceState>>()
    val getTokenBalanceCallback: LiveData<Event<GetBalanceState>>
        get() = _getBalanceStateCallback

    private val _saveSwapDataStateStateCallback = MutableLiveData<Event<SaveSwapDataState>>()
    val saveTokenSelectionCallback: LiveData<Event<SaveSwapDataState>>
        get() = _saveSwapDataStateStateCallback

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun getTokenBalance(address: String) {
        getBalanceUseCase.execute(
            Consumer {
                _getBalanceStateCallback.value = Event(
                    GetBalanceState.Success(
                        it
                    )
                )
    ,
            Consumer {
                it.printStackTrace()
                _getBalanceStateCallback.value =
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
        getWalletByAddressUseCase.dispose()
        saveSwapDataTokenUseCase.dispose()
        super.onCleared()
    }

    fun saveTokenSelection(walletAddress: String, token: Token, sourceToken: Boolean) {
        saveSwapDataTokenUseCase.execute(
            Action {
                _saveSwapDataStateStateCallback.value = Event(SaveSwapDataState.Success())
                Timber.e("Success")
    ,
            Consumer {
                it.printStackTrace()
                _saveSwapDataStateStateCallback.value =
                    Event(SaveSwapDataState.ShowError(it.localizedMessage))
    ,
            SaveSwapDataTokenUseCase.Param(walletAddress, token, sourceToken)
        )
    }

}