package com.kyberswap.android.presentation.main.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.AlertMethods
import com.kyberswap.android.domain.usecase.alert.DeleteAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertMethodsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.UpdateAlertMethodsUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertMethodsState
import com.kyberswap.android.presentation.main.profile.alert.UpdateAlertMethodsState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class AlertMethodViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val deleteAlertsUseCase: DeleteAlertsUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val getAlertMethodsUseCase: GetAlertMethodsUseCase,
    private val updateAlertMethodsUseCase: UpdateAlertMethodsUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase) {


    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    private val _getAlertMethodsCallback = MutableLiveData<Event<GetAlertMethodsState>>()
    val getAlertMethodsCallback: LiveData<Event<GetAlertMethodsState>>
        get() = _getAlertMethodsCallback

    private val _updateAlertMethodsCallback = MutableLiveData<Event<UpdateAlertMethodsState>>()
    val updateAlertMethodsCallback: LiveData<Event<UpdateAlertMethodsState>>
        get() = _updateAlertMethodsCallback


    fun getAlertMethods() {
        _getAlertMethodsCallback.postValue(Event(GetAlertMethodsState.Loading))
        getAlertMethodsUseCase.execute(
            Consumer {
                if (it.success) {
                    _getAlertMethodsCallback.value = Event(GetAlertMethodsState.Success(it.data))
                } else {
                    _getAlertMethodsCallback.value = Event(GetAlertMethodsState.ShowError(""))
                }
            },
            Consumer {
                it.printStackTrace()
                _getAlertMethodsCallback.value =
                    Event(GetAlertMethodsState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun updateAlertMethods(alertMethod: AlertMethods) {
        _updateAlertMethodsCallback.postValue(Event(UpdateAlertMethodsState.Loading))
        updateAlertMethodsUseCase.execute(
            Consumer {
                if (it.success) {
                    _updateAlertMethodsCallback.value = Event(UpdateAlertMethodsState.Success(it))
                } else {
                    _updateAlertMethodsCallback.value =
                        Event(UpdateAlertMethodsState.ShowError(it.message))
                }
            },
            Consumer {
                it.printStackTrace()
                _updateAlertMethodsCallback.value =
                    Event(UpdateAlertMethodsState.ShowError(it.localizedMessage))
            },
            UpdateAlertMethodsUseCase.Param(alertMethod)
        )
    }

    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    override fun onCleared() {
        getAlertsUseCase.dispose()
        deleteAlertsUseCase.dispose()
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.dispose()
        super.onCleared()
    }


}