package com.kyberswap.android.presentation.main.profile

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.profile.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.profile.LoginUseCase
import com.kyberswap.android.domain.usecase.profile.ResetPasswordUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginSocialUseCase: LoginSocialUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase
) : ViewModel() {
    private val _loginCallback = MutableLiveData<Event<LoginState>>()
    val loginCallback: LiveData<Event<LoginState>>
        get() = _loginCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    private val _resetPasswordCallback = MutableLiveData<Event<ResetPasswordState>>()
    val resetPasswordCallback: LiveData<Event<ResetPasswordState>>
        get() = _resetPasswordCallback

    val compositeDisposable = CompositeDisposable()

    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
    ,
            null
        )
    }

    fun login(email: String, password: String) {
        _loginCallback.postValue(Event(LoginState.Loading))
        loginUseCase.execute(
            Consumer {
                _loginCallback.value =
                    Event(LoginState.Success(it, SocialInfo(type = LoginType.NORMAL)))
    ,
            Consumer {
                it.printStackTrace()
                _loginCallback.value =
                    Event(LoginState.ShowError(it.localizedMessage))
    ,
            LoginUseCase.Param(email, password)
        )
    }

    fun login(socialInfo: SocialInfo) {
        _loginCallback.postValue(Event(LoginState.Loading))
        loginSocialUseCase.execute(
            Consumer {
                _loginCallback.value = Event(LoginState.Success(it, socialInfo))
    ,
            Consumer {
                it.printStackTrace()
                _loginCallback.value =
                    Event(LoginState.ShowError(it.localizedMessage))
    ,
            LoginSocialUseCase.Param(
                socialInfo
            )
        )
    }

    fun resetPassword(email: String) {
        _resetPasswordCallback.postValue(Event(ResetPasswordState.Loading))
        resetPasswordUseCase.execute(
            Consumer {
                _resetPasswordCallback.value = Event(ResetPasswordState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _resetPasswordCallback.value =
                    Event(ResetPasswordState.ShowError(it.localizedMessage))
    ,
            ResetPasswordUseCase.Param(email)
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        loginUseCase.dispose()
        loginSocialUseCase.dispose()
        resetPasswordUseCase.dispose()
        getLoginStatusUseCase.dispose()
        super.onCleared()
    }

}

@Parcelize
enum class LoginType(val value: String) : Parcelable {
    NORMAL("normal"),
    GOOGLE("google_oauth2"),
    FACEBOOK("facebook"),
    TWITTER("twitter")
}