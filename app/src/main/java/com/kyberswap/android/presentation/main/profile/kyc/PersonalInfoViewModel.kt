package com.kyberswap.android.presentation.main.profile.kyc

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.KycInfo
import com.kyberswap.android.domain.usecase.profile.*
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ext.display
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.parcel.Parcelize
import java.util.*
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val savePersonalInfoUseCase: SavePersonalInfoUseCase,
    private val saveLocalPersonalInfoUseCase: SaveLocalPersonalInfoUseCase,
    private val resizeImageUseCase: ResizeImageUseCase,
    private val decodeBase64DecodeUseCase: Base64DecodeUseCase
) : ViewModel() {

    private val _getUserInfoCallback = MutableLiveData<Event<UserInfoState>>()
    val getUserInfoCallback: LiveData<Event<UserInfoState>>
        get() = _getUserInfoCallback

    private val _saveKycInfoCallback = MutableLiveData<Event<SaveKycInfoState>>()
    val saveKycInfoCallback: LiveData<Event<SaveKycInfoState>>
        get() = _saveKycInfoCallback


    private val _savePersonalInfoCallback = MutableLiveData<Event<SavePersonalInfoState>>()
    val savePersonalInfoCallback: LiveData<Event<SavePersonalInfoState>>
        get() = _savePersonalInfoCallback

    private val _resizeImageCallback = MutableLiveData<Event<ResizeImageState>>()
    val resizeImageCallback: LiveData<Event<ResizeImageState>>
        get() = _resizeImageCallback

    private val _decodeImageCallback = MutableLiveData<Event<DecodeBase64State>>()
    val decodeImageCallback: LiveData<Event<DecodeBase64State>>
        get() = _decodeImageCallback

    val compositeDisposable = CompositeDisposable()

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

    fun inValidDob(dob: Date): Boolean {
        return System.currentTimeMillis() / 1000 - dob.time / 1000 <= 18 * 12 * 30 * 24 * 60 * 60
    }

    fun save(kycInfo: KycInfo?) {
        if (kycInfo == null) return
        _savePersonalInfoCallback.postValue(Event(SavePersonalInfoState.Loading))
        savePersonalInfoUseCase.execute(
            Consumer {
                if (it.success) {
                    _savePersonalInfoCallback.value = Event(SavePersonalInfoState.Success(it))
         else {
                    _savePersonalInfoCallback.value =
                        Event(SavePersonalInfoState.ShowError(it.reason.display()))
        
    ,
            Consumer {
                it.printStackTrace()
                _savePersonalInfoCallback.value =
                    Event(SavePersonalInfoState.ShowError(it.localizedMessage))
    ,
            SavePersonalInfoUseCase.Param(kycInfo)
        )
    }

    fun saveLocal(kycInfo: KycInfo) {
        saveLocalPersonalInfoUseCase.execute(
            Action {
                _saveKycInfoCallback.value = Event(SaveKycInfoState.Success(""))
    ,
            Consumer {
                it.printStackTrace()
                _saveKycInfoCallback.value = Event(SaveKycInfoState.ShowError(it.localizedMessage))
    
            , SaveLocalPersonalInfoUseCase.Param(kycInfo)
        )
    }

    fun resizeImage(absolutePath: String) {
        _resizeImageCallback.postValue(Event(ResizeImageState.Loading))
        resizeImageUseCase.execute(
            Consumer {
                _resizeImageCallback.value = Event(ResizeImageState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _resizeImageCallback.value =
                    Event(ResizeImageState.ShowError(it.localizedMessage))
    ,
            ResizeImageUseCase.Param(absolutePath)
        )
    }

    fun decode(stringImage: String) {
        decodeBase64DecodeUseCase.execute(
            Consumer {
                _decodeImageCallback.value = Event(DecodeBase64State.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _decodeImageCallback.value =
                    Event(DecodeBase64State.ShowError(it.localizedMessage))
    ,
            Base64DecodeUseCase.Param(stringImage)
        )

    }

    @Parcelize
    enum class InfoType : Parcelable {
        NATIONALITY,
        COUNTRY_OF_RESIDENCE,
        PROOF_ADDRESS,
        SOURCE_FUND
    }

    override fun onCleared() {
        getUserInfoUseCase.dispose()
        savePersonalInfoUseCase.dispose()
        resizeImageUseCase.dispose()
        decodeBase64DecodeUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

}


@Parcelize
enum class KycInfoType : Parcelable {
    NATIONALITY,
    COUNTRY_OF_RESIDENCE,
    PROOF_ADDRESS,
    SOURCE_FUND,
    OCCUPATION_CODE,
    INDUSTRY_CODE,
    TAX_RESIDENCY_COUNTRY
}