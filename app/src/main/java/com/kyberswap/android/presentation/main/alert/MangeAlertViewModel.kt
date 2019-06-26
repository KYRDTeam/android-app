package com.kyberswap.android.presentation.main.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.usecase.alert.DeleteAlertsUseCase
import com.kyberswap.android.domain.usecase.alert.GetAlertsUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.profile.alert.DeleteAlertsState
import com.kyberswap.android.presentation.main.profile.alert.GetAlertsState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class MangeAlertViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val deleteAlertsUseCase: DeleteAlertsUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase) {

    private val _getAlertsCallback = MutableLiveData<Event<GetAlertsState>>()
    val getAlertsCallback: LiveData<Event<GetAlertsState>>
        get() = _getAlertsCallback

    private val _deleteAlertsCallback = MutableLiveData<Event<DeleteAlertsState>>()
    val deleteAlertsCallback: LiveData<Event<DeleteAlertsState>>
        get() = _deleteAlertsCallback


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

    fun deleteAlert(alert: Alert) {
        deleteAlertsUseCase.execute(
            Consumer {
                _deleteAlertsCallback.value = Event(DeleteAlertsState.Success(it))
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