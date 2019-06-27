package com.kyberswap.android.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.usecase.alert.DeleteAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertsUseCase
import com.kyberswap.android.domain.usecase.profile.LogoutUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ProfileDetailViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAlertsUseCase: DeleteAlertsUseCase
) : ViewModel() {

    private val _getAlertsCallback = MutableLiveData<Event<GetAlertsState>>()
    val getAlertsCallback: LiveData<Event<GetAlertsState>>
        get() = _getAlertsCallback

    private val _deleteAlertsCallback = MutableLiveData<Event<DeleteAlertsState>>()
    val deleteAlertsCallback: LiveData<Event<DeleteAlertsState>>
        get() = _deleteAlertsCallback

    private val _logoutCallback = MutableLiveData<Event<LogoutState>>()
    val logoutCallback: LiveData<Event<LogoutState>>
        get() = _logoutCallback


    fun getAlert() {
        getAlertsUseCase.execute(
            Consumer {
                _getAlertsCallback.value = Event(GetAlertsState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getAlertsCallback.value =
                    Event(GetAlertsState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun logout() {
        _logoutCallback.postValue(Event(LogoutState.Loading))
        logoutUseCase.execute(
            Action {
                _logoutCallback.value = Event(LogoutState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _logoutCallback.value =
                    Event(LogoutState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun deleteAlert(alert: Alert) {
        deleteAlertsUseCase.execute(
            Consumer {
                _deleteAlertsCallback.value = Event(DeleteAlertsState.Success())
            },
            Consumer {
                it.printStackTrace()
                _deleteAlertsCallback.value =
                    Event(DeleteAlertsState.ShowError(it.localizedMessage))
            },
            DeleteAlertsUseCase.Param(alert)
        )
    }
}