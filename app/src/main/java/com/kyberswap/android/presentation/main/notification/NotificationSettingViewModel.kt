package com.kyberswap.android.presentation.main.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.notification.GetSubscriptionNotificationUseCase
import com.kyberswap.android.domain.usecase.notification.TogglePriceNotificationUseCase
import com.kyberswap.android.domain.usecase.notification.UpdateSubscribedTokenNotificationUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class NotificationSettingViewModel @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val togglePriceNotificationUseCase: TogglePriceNotificationUseCase,
    private val getSubscriptionNotification: GetSubscriptionNotificationUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val updateSubscribedTokenNotification: UpdateSubscribedTokenNotificationUseCase
) : ViewModel() {

    private val _getSubscribedNotificationsCallback =
        MutableLiveData<Event<GetSubscriptionNotificationState>>()
    val getSubscribedNotificationsCallback: LiveData<Event<GetSubscriptionNotificationState>>
        get() = _getSubscribedNotificationsCallback

    private val _togglePriceNotificationsCallback =
        MutableLiveData<Event<TogglePriceNotificationState>>()
    val togglePriceNotificationsCallback: LiveData<Event<TogglePriceNotificationState>>
        get() = _togglePriceNotificationsCallback

    private val _updateSubscribedTokensNotificationsCallback =
        MutableLiveData<Event<UpdateSubscribedTokensNotificationState>>()
    val updateSubscribedTokensNotificationsCallback: LiveData<Event<UpdateSubscribedTokensNotificationState>>
        get() = _updateSubscribedTokensNotificationsCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    fun getSubscriptionTokens() {
        getSubscriptionNotification.dispose()
        getSubscriptionNotification.execute(
            Consumer {
                if (it.success) {
                    _getSubscribedNotificationsCallback.value =
                        Event(GetSubscriptionNotificationState.Success(it.data))
                } else {
                    _getSubscribedNotificationsCallback.value =
                        Event(GetSubscriptionNotificationState.ShowError(it.message))
                }
            },
            Consumer {
                it.printStackTrace()
                _getSubscribedNotificationsCallback.value =
                    Event(GetSubscriptionNotificationState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    override fun onCleared() {
        getSubscriptionNotification.dispose()
        super.onCleared()
    }

    fun togglePriceNoti(checked: Boolean) {
        togglePriceNotificationUseCase.dispose()
        togglePriceNotificationUseCase.execute(
            Consumer {
                if (it.success) {
                    _togglePriceNotificationsCallback.value =
                        Event(TogglePriceNotificationState.Success())
                } else {
                    _togglePriceNotificationsCallback.value =
                        Event(TogglePriceNotificationState.ShowError(it.message))
                }
            },
            Consumer {
                it.printStackTrace()
                _togglePriceNotificationsCallback.value =
                    Event(TogglePriceNotificationState.ShowError(errorHandler.getError(it)))
            },
            checked
        )
    }

    fun updateSubscribedTokens(tokens: List<String>) {
        updateSubscribedTokenNotification.dispose()
        updateSubscribedTokenNotification.execute(
            Consumer {
                if (it.success) {
                    _updateSubscribedTokensNotificationsCallback.value =
                        Event(UpdateSubscribedTokensNotificationState.Success())
                } else {
                    _updateSubscribedTokensNotificationsCallback.value =
                        Event(UpdateSubscribedTokensNotificationState.ShowError(it.message))
                }
            },
            Consumer {
                it.printStackTrace()
                _updateSubscribedTokensNotificationsCallback.value =
                    Event(UpdateSubscribedTokensNotificationState.ShowError(errorHandler.getError(it)))
            },
            UpdateSubscribedTokenNotificationUseCase.Param(tokens)
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
}