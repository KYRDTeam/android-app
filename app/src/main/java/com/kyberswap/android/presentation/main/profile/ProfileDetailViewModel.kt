package com.kyberswap.android.presentation.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.usecase.alert.DeleteAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertsUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.profile.LogoutUseCase
import com.kyberswap.android.domain.usecase.profile.ReSubmitUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.UpdatePushTokenUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class ProfileDetailViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val deleteAlertsUseCase: DeleteAlertsUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val reSubmitUserInfoUseCase: ReSubmitUserInfoUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val errorHandler: ErrorHandler
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

    private val _getUserInfoCallback = MutableLiveData<Event<UserInfoState>>()
    val getUserInfoCallback: LiveData<Event<UserInfoState>>
        get() = _getUserInfoCallback

    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getUserInfoCallback.value = Event(UserInfoState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getUserInfoCallback.value =
                    Event(UserInfoState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun updatePushToken(userId: String, token: String?) {
        updatePushTokenUseCase.dispose()
        updatePushTokenUseCase.execute(
            Consumer {

            },
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
            },
            UpdatePushTokenUseCase.Param(
                userId,
                token
            )
        )
    }

    fun getAlert() {
        getAlertsUseCase.execute(
            Consumer {
                _getAlertsCallback.value = Event(GetAlertsState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getAlertsCallback.value =
                    Event(GetAlertsState.ShowError(errorHandler.getError(it)))
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
                    Event(LogoutState.ShowError(errorHandler.getError(it)))
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
                    Event(DeleteAlertsState.ShowError(errorHandler.getError(it)))
            },
            DeleteAlertsUseCase.Param(alert)
        )
    }

    override fun onCleared() {
        getAlertsUseCase.dispose()
        logoutUseCase.dispose()
        deleteAlertsUseCase.dispose()
        getLoginStatusUseCase.dispose()
        updatePushTokenUseCase.dispose()
        reSubmitUserInfoUseCase.dispose() //        refreshUserInfoUseCase.dispose()
        super.onCleared()
    }

}