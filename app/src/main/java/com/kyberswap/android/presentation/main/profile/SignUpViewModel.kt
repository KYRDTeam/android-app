package com.kyberswap.android.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.usecase.profile.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.profile.SignUpUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val loginSocialUseCase: LoginSocialUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _signUpCallback = MutableLiveData<Event<SignUpState>>()
    val signUpCallback: LiveData<Event<SignUpState>>
        get() = _signUpCallback


    private val _loginCallback = MutableLiveData<Event<LoginState>>()
    val loginCallback: LiveData<Event<LoginState>>
        get() = _loginCallback

    fun signUp(email: String, displayName: String, password: String, isSubscription: Boolean) {
        _signUpCallback.postValue(Event(SignUpState.Loading))
        signUpUseCase.execute(
            Consumer {
                _signUpCallback.value = Event(SignUpState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _signUpCallback.value =
                    Event(SignUpState.ShowError(it.localizedMessage))
            },
            SignUpUseCase.Param(email, displayName, password, isSubscription)
        )
    }


    fun login(socialInfo: SocialInfo) {
        _loginCallback.postValue(Event(LoginState.Loading))
        loginSocialUseCase.execute(
            Consumer {
                _loginCallback.value = Event(LoginState.Success(it, socialInfo))
            },
            Consumer {
                it.printStackTrace()
                _loginCallback.value =
                    Event(LoginState.ShowError(it.localizedMessage))
            },
            LoginSocialUseCase.Param(
                socialInfo
            )
        )
    }


}