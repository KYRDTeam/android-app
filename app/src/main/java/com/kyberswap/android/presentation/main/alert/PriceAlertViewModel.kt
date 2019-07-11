package com.kyberswap.android.presentation.main.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.usecase.alert.CreateOrUpdateAlertUseCase
import com.kyberswap.android.domain.usecase.alert.GetCurrentAlertUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import javax.inject.Inject

class PriceAlertViewModel @Inject constructor(
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val getCurrentAlertUseCase: GetCurrentAlertUseCase,
    private val createOrUpdateAlertUseCase: CreateOrUpdateAlertUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase) {

    val compositeDisposable = CompositeDisposable()

    private val _getCurrentAlertCallback = MutableLiveData<Event<GetCurrentAlertState>>()
    val getCurrentAlertCallback: LiveData<Event<GetCurrentAlertState>>
        get() = _getCurrentAlertCallback

    private val _createOrUpdateAlertCallback = MutableLiveData<Event<CreateOrUpdateAlertState>>()
    val createOrUpdateAlertCallback: LiveData<Event<CreateOrUpdateAlertState>>
        get() = _createOrUpdateAlertCallback

    fun getCurrentAlert(walletAddress: String, alert: Alert?) {
        getCurrentAlertUseCase.execute(
            Consumer {
                _getCurrentAlertCallback.value = Event(GetCurrentAlertState.Success(it))

            },
            Consumer {
                it.printStackTrace()
                _getCurrentAlertCallback.value =
                    Event(GetCurrentAlertState.ShowError(it.localizedMessage))
            },
            GetCurrentAlertUseCase.Param(walletAddress, alert)
        )
    }

    fun createOrUpdateAlert(alert: Alert?) {
        if (alert == null) return
        _createOrUpdateAlertCallback.postValue(Event(CreateOrUpdateAlertState.Loading))
        createOrUpdateAlertUseCase.execute(
            Consumer {
                if (it.message.isNullOrEmpty()) {
                    _createOrUpdateAlertCallback.value = Event(CreateOrUpdateAlertState.Success(it))
                } else {
                    _createOrUpdateAlertCallback.value =
                        Event(CreateOrUpdateAlertState.ShowError(it.message))
                }
            },
            Consumer {
                it.printStackTrace()
                _createOrUpdateAlertCallback.value =
                    Event(CreateOrUpdateAlertState.ShowError(it.localizedMessage))
            },
            CreateOrUpdateAlertUseCase.Param(alert)
        )

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }


}
