package com.kyberswap.android.presentation.main.profile.kyc

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.KycInfo
import com.kyberswap.android.domain.usecase.profile.Base64DecodeUseCase
import com.kyberswap.android.domain.usecase.profile.GetUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.ResizeImageUseCase
import com.kyberswap.android.domain.usecase.profile.SaveIdPassportUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ext.display
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PassportViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val resizeImageUseCase: ResizeImageUseCase,
    private val decodeBase64DecodeUseCase: Base64DecodeUseCase,
    private val saveIdPassportUseCase: SaveIdPassportUseCase
) : ViewModel() {

    val compositeDisposable = CompositeDisposable()

    private val _resizeImageCallback = MutableLiveData<Event<ResizeImageState>>()
    val resizeImageCallback: LiveData<Event<ResizeImageState>>
        get() = _resizeImageCallback

    private val _decodeImageCallback = MutableLiveData<Event<DecodeBase64State>>()
    val decodeImageCallback: LiveData<Event<DecodeBase64State>>
        get() = _decodeImageCallback

    private val _getUserInfoCallback = MutableLiveData<Event<UserInfoState>>()
    val getUserInfoCallback: LiveData<Event<UserInfoState>>
        get() = _getUserInfoCallback

    private val _savePersonalInfoCallback = MutableLiveData<Event<SavePersonalInfoState>>()
    val savePersonalInfoCallback: LiveData<Event<SavePersonalInfoState>>
        get() = _savePersonalInfoCallback


    fun getUserInfo() {
        getUserInfoUseCase.dispose()
        getUserInfoUseCase.execute(
            Consumer {
                _getUserInfoCallback.value = Event(UserInfoState.Success(it))

            },
            Consumer {
                it.printStackTrace()
                _getUserInfoCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun resizeImage(absolutePath: String) {
        _resizeImageCallback.postValue(Event(ResizeImageState.Loading))
        resizeImageUseCase.execute(
            Consumer {
                _resizeImageCallback.value = Event(ResizeImageState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _resizeImageCallback.value =
                    Event(ResizeImageState.ShowError(it.localizedMessage))
            },
            ResizeImageUseCase.Param(absolutePath)
        )
    }

    fun decode(stringImage: String, imageView: ImageView?) {
        decodeBase64DecodeUseCase.execute(
            Consumer {
                _decodeImageCallback.value = Event(DecodeBase64State.Success(it, imageView))
            },
            Consumer {
                it.printStackTrace()
                _decodeImageCallback.value =
                    Event(DecodeBase64State.ShowError(it.localizedMessage))
            },
            Base64DecodeUseCase.Param(stringImage)
        )

    }

    fun save(kycInfo: KycInfo) {
        _savePersonalInfoCallback.postValue(Event(SavePersonalInfoState.Loading))
        saveIdPassportUseCase.execute(
            Consumer {
                if (it.success) {
                    _savePersonalInfoCallback.value = Event(SavePersonalInfoState.Success(it))
                } else {
                    _savePersonalInfoCallback.value =
                        Event(SavePersonalInfoState.ShowError(it.reason.display()))
                }
            },
            Consumer {
                it.printStackTrace()
                _savePersonalInfoCallback.value =
                    Event(SavePersonalInfoState.ShowError(it.localizedMessage))
            },
            SaveIdPassportUseCase.Param(kycInfo)
        )

    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}