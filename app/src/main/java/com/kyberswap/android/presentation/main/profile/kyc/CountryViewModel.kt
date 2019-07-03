package com.kyberswap.android.presentation.main.profile.kyc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.profile.SaveKycInfoUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class CountryViewModel @Inject constructor(
    private val saveKycInfoUseCase: SaveKycInfoUseCase
) : ViewModel() {

    private val _saveKycInfoCallback = MutableLiveData<Event<SaveKycInfoState>>()
    val saveKycInfoCallback: LiveData<Event<SaveKycInfoState>>
        get() = _saveKycInfoCallback

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    fun save(value: String, kycInfoType: KycInfoType) {
        saveKycInfoUseCase.execute(
            Action {
                _saveKycInfoCallback.value = Event(SaveKycInfoState.Success(""))

    ,
            Consumer {

                it.printStackTrace()
                _saveKycInfoCallback.value = Event(SaveKycInfoState.ShowError(""))
    ,
            SaveKycInfoUseCase.Param(value, kycInfoType)
        )
    }

}