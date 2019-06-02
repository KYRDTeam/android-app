package com.kyberswap.android.presentation.main.profile

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.usecase.wallet.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.wallet.LoginUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginSocialUseCase: LoginSocialUseCase
) : ViewModel() {
    private val _loginCallback = MutableLiveData<Event<LoginState>>()
    val loginCallback: LiveData<Event<LoginState>>
        get() = _loginCallback

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

}

@Parcelize
enum class LoginType(val value: String) : Parcelable {
    NORMAL("normal"),
    GOOGLE("google_oauth2"),
    FACEBOOK("facebook"),
    TWITTER("twitter")
}