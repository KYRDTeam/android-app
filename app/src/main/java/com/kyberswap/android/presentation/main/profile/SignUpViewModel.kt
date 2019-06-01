package com.kyberswap.android.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.wallet.SignUpUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _signUpCallback = MutableLiveData<Event<SignUpState>>()
    val signUpCallback: LiveData<Event<SignUpState>>
        get() = _signUpCallback

    fun signUp(email: String, displayName: String, password: String, isSubscription: Boolean) {
        _signUpCallback.postValue(Event(SignUpState.Loading))
        signUpUseCase.execute(
            Consumer {
                _signUpCallback.value = Event(SignUpState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _signUpCallback.value =
                    Event(SignUpState.ShowError(it.localizedMessage))
    ,
            SignUpUseCase.Param(email, displayName, password, isSubscription)
        )
    }


}