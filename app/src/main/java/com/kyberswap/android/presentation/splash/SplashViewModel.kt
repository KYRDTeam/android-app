package com.kyberswap.android.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SplashViewModel @Inject constructor(private val getWalletUseCase: GetSelectedWalletUseCase) :
    ViewModel() {

    private val _getWalletStateCallback = MutableLiveData<Event<GetWalletState>>()
    val getWalletStateCallback: LiveData<Event<GetWalletState>>
        get() = _getWalletStateCallback

    fun getWallet() {
        getWalletUseCase.execute(
            Consumer {
                _getWalletStateCallback.value = Event(GetWalletState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getWalletStateCallback.value = Event(GetWalletState.ShowError(it.localizedMessage))
    ,
            null
        )
    }
}