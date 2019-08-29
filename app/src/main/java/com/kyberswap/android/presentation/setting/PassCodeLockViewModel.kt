package com.kyberswap.android.presentation.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.setting.GetPinUseCase
import com.kyberswap.android.domain.usecase.setting.SavePinUseCase
import com.kyberswap.android.domain.usecase.setting.VerifyPinUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PassCodeLockViewModel @Inject constructor(
    private val savePinUseCase: SavePinUseCase,
    private val verifyPinUseCase: VerifyPinUseCase,
    private val getPinUseCase: GetPinUseCase,
    private val errorHandler: ErrorHandler

) : ViewModel() {

    private val _savePinCallback = MutableLiveData<Event<SavePinState>>()
    val savePinCallback: LiveData<Event<SavePinState>>
        get() = _savePinCallback

    private val _verifyPinCallback = MutableLiveData<Event<VerifyPinState>>()
    val verifyPinCallback: LiveData<Event<VerifyPinState>>
        get() = _verifyPinCallback

    private val _getPinCallback = MutableLiveData<Event<GetPinState>>()
    val getPinCallback: LiveData<Event<GetPinState>>
        get() = _getPinCallback

    val compositeDisposable = CompositeDisposable()


    fun save(pin: String) {
        savePinUseCase.execute(
            Action {
                _savePinCallback.value = Event(SavePinState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _savePinCallback.value = Event(SavePinState.ShowError(errorHandler.getError(it)))
            },
            SavePinUseCase.Param(pin)
        )

    }

    fun getPin() {
        getPinUseCase.execute(
            Consumer {
                _getPinCallback.value = Event(GetPinState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getPinCallback.value = Event(GetPinState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }


    fun verifyPin(pin: String, remainNum: Int, time: Long) {
        verifyPinUseCase.execute(
            Consumer {
                _verifyPinCallback.value = Event(VerifyPinState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _verifyPinCallback.value =
                    Event(VerifyPinState.ShowError(errorHandler.getError(it)))
            },
            VerifyPinUseCase.Param(pin, remainNum, time)
        )
    }

    public override fun onCleared() {
        compositeDisposable.dispose()
        savePinUseCase.dispose()
        verifyPinUseCase.dispose()
        getPinUseCase.dispose()
        super.onCleared()
    }

}