package com.kyberswap.android.presentation.main.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.usecase.notification.GetNotificationUseCase
import com.kyberswap.android.domain.usecase.notification.ReadNotificationsUseCase
import com.kyberswap.android.domain.usecase.profile.GetUserInfoUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val errorHandler: ErrorHandler,
    private val notificationUseCase: GetNotificationUseCase,
    private val readNotificationsUseCase: ReadNotificationsUseCase,
    private val getLoginStatusUseCase: GetUserInfoUseCase
) : ViewModel() {

    private val _getNotificationsCallback = MutableLiveData<Event<GetNotificationsState>>()
    val getNotificationsCallback: LiveData<Event<GetNotificationsState>>
        get() = _getNotificationsCallback

    private val _readNotificationsCallback = MutableLiveData<Event<ReadNotificationsState>>()
    val readNotificationsCallback: LiveData<Event<ReadNotificationsState>>
        get() = _readNotificationsCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback


    fun getNotifications() {
        notificationUseCase.dispose()
        _getNotificationsCallback.postValue(Event(GetNotificationsState.Loading))
        notificationUseCase.execute(
            Consumer {
                _getNotificationsCallback.value = Event(GetNotificationsState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getNotificationsCallback.value =
                    Event(GetNotificationsState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun readNotification(notification: Notification) {
        _readNotificationsCallback.postValue(Event(ReadNotificationsState.Loading))
        readNotificationsUseCase.execute(
            Consumer {
                if (it.success) {
                    _readNotificationsCallback.value =
                        Event(
                            ReadNotificationsState.Success(
                                it.success,
                                notification.copy(read = true)
                            )
                        )
                } else {
                    _readNotificationsCallback.value =
                        Event(ReadNotificationsState.ShowError(it.message))
                }
            },
            Consumer {
                _readNotificationsCallback.value =
                    Event(ReadNotificationsState.ShowError(errorHandler.getError(it)))
            },
            ReadNotificationsUseCase.Param(listOf(notification))
        )
    }

    fun readAll(notifications: List<Notification>) {
        _readNotificationsCallback.postValue(Event(ReadNotificationsState.Loading))
        readNotificationsUseCase.execute(
            Consumer {
                if (it.success) {
                    _readNotificationsCallback.value =
                        Event(
                            ReadNotificationsState.Success(
                                it.success, isReadAll = true
                            )
                        )
                } else {
                    _readNotificationsCallback.value =
                        Event(ReadNotificationsState.ShowError(it.message))
                }
            },
            Consumer {
                _readNotificationsCallback.value =
                    Event(ReadNotificationsState.ShowError(errorHandler.getError(it)))
            },
            ReadNotificationsUseCase.Param(notifications)
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