package com.kyberswap.android.presentation.main.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.usecase.explore.GetCampaignUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ExploreViewModel @Inject constructor(
    private val getCampaignUseCase: GetCampaignUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _getCampaignStateCallback = MutableLiveData<Event<GetCampaignsState>>()
    val getCampaignStateCallback: LiveData<Event<GetCampaignsState>>
        get() = _getCampaignStateCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    fun getCampaign() {
        getCampaignUseCase.execute(
            Consumer {
                _getCampaignStateCallback.value = Event(GetCampaignsState.Success(it))
            },
            Consumer {
                _getCampaignStateCallback.value =
                    Event(GetCampaignsState.ShowError(it.localizedMessage))
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
        getCampaignUseCase.dispose()
        super.onCleared()
    }
}