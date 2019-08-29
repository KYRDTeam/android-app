package com.kyberswap.android.presentation.main.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.usecase.alert.DeleteAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertsUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class MangeAlertViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val deleteAlertsUseCase: DeleteAlertsUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _getAlertsCallback = MutableLiveData<Event<GetAlertsState>>()
    val getAlertsCallback: LiveData<Event<GetAlertsState>>
        get() = _getAlertsCallback

    private val _deleteAlertsCallback = MutableLiveData<Event<DeleteAlertsState>>()
    val deleteAlertsCallback: LiveData<Event<DeleteAlertsState>>
        get() = _deleteAlertsCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback


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

    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(errorHandler.getError(it)))
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

}