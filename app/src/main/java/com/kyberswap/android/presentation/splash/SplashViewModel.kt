package com.kyberswap.android.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.PreloadUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val preloadUseCase: PreloadUseCase
) :
    ViewModel() {

    private val _getWalletStateCallback = MutableLiveData<Event<GetUserWalletState>>()
    val getWalletStateCallback: LiveData<Event<GetUserWalletState>>
        get() = _getWalletStateCallback

    fun prepareData() {
        preloadUseCase.dispose()
        _getWalletStateCallback.postValue(Event(GetUserWalletState.Loading))
        preloadUseCase.execute(
            Consumer {
                _getWalletStateCallback.value =
                    Event(GetUserWalletState.Success(it.second, it.first))
                preloadUseCase.dispose()


            },
            Consumer {
                it.printStackTrace()
                _getWalletStateCallback.value =
                    Event(GetUserWalletState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    override fun onCleared() {
        preloadUseCase.dispose()
        super.onCleared()
    }
}