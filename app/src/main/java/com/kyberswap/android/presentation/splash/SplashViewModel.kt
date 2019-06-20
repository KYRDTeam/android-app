package com.kyberswap.android.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.UserInfoState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val getWalletUseCase: GetSelectedWalletUseCase,
    private val prepareBalanceUseCase: PrepareBalanceUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase
) :
    ViewModel() {

    private val _getWalletStateCallback = MutableLiveData<Event<GetUserWalletState>>()
    val getWalletStateCallback: LiveData<Event<GetUserWalletState>>
        get() = _getWalletStateCallback

    fun getWallet(userInfo: UserInfo?) {
        getWalletUseCase.execute(
            Consumer { wallet ->
                _getWalletStateCallback.value = Event(GetUserWalletState.Success(wallet, userInfo))

            },
            Consumer {
                it.printStackTrace()
                _getWalletStateCallback.value =
                    Event(GetUserWalletState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    private fun verifyLoginStatus() {
        getLoginStatusUseCase.execute(
            Consumer {
                getWallet(it)
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))

            },
            Consumer {
                getWallet(null)
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun prepareData() {
        prepareBalanceUseCase.execute(
            Consumer {
                verifyLoginStatus()
            },
            Consumer { error ->
                error.printStackTrace()
                verifyLoginStatus()
            },
            PrepareBalanceUseCase.Param()
        )
    }

    override fun onCleared() {
        prepareBalanceUseCase.dispose()
        super.onCleared()
    }
}