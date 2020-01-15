package com.kyberswap.android.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.usecase.profile.DataTransferUseCase
import com.kyberswap.android.domain.usecase.profile.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.profile.LogoutUseCase
import com.kyberswap.android.domain.usecase.profile.SignUpUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val loginSocialUseCase: LoginSocialUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val dataTransferUseCase: DataTransferUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _signUpCallback = MutableLiveData<Event<SignUpState>>()
    val signUpCallback: LiveData<Event<SignUpState>>
        get() = _signUpCallback

    private val _loginCallback = MutableLiveData<Event<LoginState>>()
    val loginCallback: LiveData<Event<LoginState>>
        get() = _loginCallback

    private val _logoutCallback = MutableLiveData<Event<LogoutState>>()
    val logoutCallback: LiveData<Event<LogoutState>>
        get() = _logoutCallback

    private val _dataTransferCallback = MutableLiveData<Event<DataTransferState>>()
    val dataTransferCallback: LiveData<Event<DataTransferState>>
        get() = _dataTransferCallback

    val compositeDisposable = CompositeDisposable()

    fun signUp(email: String, displayName: String, password: String, isSubscription: Boolean) {
        _signUpCallback.postValue(Event(SignUpState.Loading))
        signUpUseCase.execute(
            Consumer {
                _signUpCallback.value = Event(SignUpState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _signUpCallback.value =
                    Event(SignUpState.ShowError(errorHandler.getError(it)))
            },
            SignUpUseCase.Param(email, displayName, password, isSubscription)
        )
    }


    fun login(socialInfo: SocialInfo, isConfirm: Boolean = false) {
        _loginCallback.postValue(Event(LoginState.Loading))
        loginSocialUseCase.execute(
            Consumer {
                _loginCallback.value = Event(LoginState.Success(it, socialInfo))
            },
            Consumer {
                it.printStackTrace()
                _loginCallback.value =
                    Event(LoginState.ShowError(errorHandler.getError(it)))
            },
            LoginSocialUseCase.Param(
                socialInfo, isConfirm
            )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        loginSocialUseCase.dispose()
        signUpUseCase.dispose()
        logoutUseCase.dispose()
        super.onCleared()
    }

    fun logout() {
        _logoutCallback.postValue(Event(LogoutState.Loading))
        logoutUseCase.execute(
            Action {
                _logoutCallback.value = Event(LogoutState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _logoutCallback.value =
                    Event(LogoutState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun transfer(action: String, userInfo: UserInfo) {
        dataTransferUseCase.execute(
            Consumer {
                _dataTransferCallback.value =
                    Event(DataTransferState.Success(it, userInfo.copy(transferPermission = action)))
            },
            Consumer {
                _dataTransferCallback.value =
                    Event(
                        DataTransferState.ShowError(
                            errorHandler.getError(it),
                            userInfo.copy(transferPermission = action)
                        )
                    )
                it.printStackTrace()
            },
            DataTransferUseCase.Param(action)
        )
    }
}