package com.kyberswap.android.presentation.main.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.usecase.alert.GetLeaderBoardAlertsUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.profile.alert.GetLeaderBoardState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LeaderBoardViewModel @Inject constructor(
    private val getLeaderBoardAlertsUseCase: GetLeaderBoardAlertsUseCase,

    getSelectedWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase) {

    private val _getAlertsCallback = MutableLiveData<Event<GetLeaderBoardState>>()
    val getAlertsCallback: LiveData<Event<GetLeaderBoardState>>
        get() = _getAlertsCallback

    fun getLeaderBoard() {
        _getAlertsCallback.postValue(Event(GetLeaderBoardState.Loading))
        getLeaderBoardAlertsUseCase.execute(
            Consumer { lb ->
                val meAlert = lb.currentUserEntity.activeAlerts.map {
                    it.copy(rank = lb.currentUserEntity.rank)
        .firstOrNull()
                val alerts = mutableListOf<Alert>()
                if (meAlert != null) {
                    alerts.add(meAlert)
        
                alerts.addAll(lb.data)

                _getAlertsCallback.value =
                    Event(GetLeaderBoardState.Success(alerts, lb.campaignInfo))
    ,
            Consumer {
                it.printStackTrace()
                _getAlertsCallback.value =
                    Event(GetLeaderBoardState.ShowError(it.localizedMessage))
    ,
            null
        )
    }

}