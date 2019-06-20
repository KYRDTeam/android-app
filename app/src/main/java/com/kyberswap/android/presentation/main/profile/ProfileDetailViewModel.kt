package com.kyberswap.android.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.profile.LogoutUseCase
import com.kyberswap.android.domain.usecase.profile.alert.GetAlertUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ProfileDetailViewModel @Inject constructor(
    private val getAlertUseCase: GetAlertUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _getAlertsCallback = MutableLiveData<Event<GetAlertsState>>()
    val getAlertsCallback: LiveData<Event<GetAlertsState>>
        get() = _getAlertsCallback

    private val _logoutCallback = MutableLiveData<Event<LogoutState>>()
    val logoutCallback: LiveData<Event<LogoutState>>
        get() = _logoutCallback


    fun getAlert() {
        getAlertUseCase.execute(
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
}