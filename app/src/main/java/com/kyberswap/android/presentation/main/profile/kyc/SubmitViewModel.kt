package com.kyberswap.android.presentation.main.profile.kyc

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.usecase.profile.Base64DecodeUseCase
import com.kyberswap.android.domain.usecase.profile.GetUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.SubmitUserInfoUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ext.display
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SubmitViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val decodeBase64DecodeUseCase: Base64DecodeUseCase,
    private val submitUserInfoUseCase: SubmitUserInfoUseCase
) : ViewModel() {

    private val _getUserInfoCallback = MutableLiveData<Event<UserInfoState>>()
    val getUserInfoCallback: LiveData<Event<UserInfoState>>
        get() = _getUserInfoCallback

    private val _decodeImageCallback = MutableLiveData<Event<DecodeBase64State>>()
    val decodeImageCallback: LiveData<Event<DecodeBase64State>>
        get() = _decodeImageCallback

    private val _submitUserInfoCallback = MutableLiveData<Event<SavePersonalInfoState>>()
    val submitUserInfoCallback: LiveData<Event<SavePersonalInfoState>>
        get() = _submitUserInfoCallback


    fun getUserInfo() {
        getUserInfoUseCase.dispose()
        getUserInfoUseCase.execute(
            Consumer {
                _getUserInfoCallback.value = Event(UserInfoState.Success(it))

    ,
            Consumer {
                it.printStackTrace()
                _getUserInfoCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
    ,
            null
        )
    }

    fun decode(stringImage: String, imageView: ImageView?) {
        decodeBase64DecodeUseCase.execute(
            Consumer {
                _decodeImageCallback.value = Event(DecodeBase64State.Success(it, imageView))
    ,
            Consumer {
                it.printStackTrace()
                _decodeImageCallback.value =
                    Event(DecodeBase64State.ShowError(it.localizedMessage))
    ,
            Base64DecodeUseCase.Param(stringImage)
        )

    }

    fun submit(user: UserInfo) {
        submitUserInfoUseCase.execute(
            Consumer {
                if (it.success) {
                    _submitUserInfoCallback.value = Event(SavePersonalInfoState.Success(it))
         else {
                    _submitUserInfoCallback.value =
                        Event(SavePersonalInfoState.ShowError(it.reason.display()))
        
    ,
            Consumer {
                it.printStackTrace()
                _submitUserInfoCallback.value =
                    Event(SavePersonalInfoState.ShowError(it.localizedMessage))
    ,
            SubmitUserInfoUseCase.Param(user)
        )
    }

}