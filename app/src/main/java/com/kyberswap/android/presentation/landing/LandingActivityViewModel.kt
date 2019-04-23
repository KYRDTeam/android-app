package com.kyberswap.android.presentation.landing

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject


class LandingActivityViewModel @Inject constructor(
    private val createWalletUseCase: CreateWalletUseCase
) : ViewModel() {


    private val _createWalletCallback = MutableLiveData<Event<CreateWalletState>>()
    val createWalletCallback: LiveData<Event<CreateWalletState>>
        get() = _createWalletCallback

    fun createWallet(pinLock: String = "") {
        _createWalletCallback.postValue(Event(CreateWalletState.Loading))
        createWalletUseCase.execute(
            Consumer {

                _createWalletCallback.value = Event(CreateWalletState.Success(it))

    ,
            Consumer {
                it.printStackTrace()
                _createWalletCallback.value =
                    Event(CreateWalletState.ShowError(it.localizedMessage))
    ,
            CreateWalletUseCase.Param(pinLock)
        )
    }

    override fun onCleared() {
        createWalletUseCase.dispose()
        super.onCleared()
    }

}