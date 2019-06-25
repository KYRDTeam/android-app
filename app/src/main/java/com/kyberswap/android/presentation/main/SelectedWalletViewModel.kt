package com.kyberswap.android.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.splash.GetWalletState
import io.reactivex.functions.Consumer
import javax.inject.Inject

open class SelectedWalletViewModel @Inject constructor(
    private val getWalletUseCase: GetSelectedWalletUseCase
) : ViewModel() {

    private val _getSelectedWalletCallback = MutableLiveData<Event<GetWalletState>>()
    val getSelectedWalletCallback: LiveData<Event<GetWalletState>>
        get() = _getSelectedWalletCallback

    fun getSelectedWallet() {
        getWalletUseCase.execute(
            Consumer { wallet ->
                _getSelectedWalletCallback.value = Event(GetWalletState.Success(wallet))

            },
            Consumer {
                it.printStackTrace()
                _getSelectedWalletCallback.value =
                    Event(GetWalletState.ShowError(it.localizedMessage))
            },
            null
        )
    }
}