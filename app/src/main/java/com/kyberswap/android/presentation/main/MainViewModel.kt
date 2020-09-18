package com.kyberswap.android.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.RatingInfo
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.balance.UpdateBalanceUseCase
import com.kyberswap.android.domain.usecase.notification.GetUnReadNotificationUseCase
import com.kyberswap.android.domain.usecase.notification.ReadNotificationsUseCase
import com.kyberswap.android.domain.usecase.profile.DataTransferUseCase
import com.kyberswap.android.domain.usecase.profile.GetRatingUseCase
import com.kyberswap.android.domain.usecase.profile.GetUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.LogoutUseCase
import com.kyberswap.android.domain.usecase.profile.RefreshUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.SaveRatingInfoUseCase
import com.kyberswap.android.domain.usecase.profile.UpdatePushTokenUseCase
import com.kyberswap.android.domain.usecase.swap.GetMaxGasPriceUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetOtherBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetTokenBalanceUseCase
import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.domain.usecase.transaction.GetPendingTransactionsUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsPeriodicallyUseCase
import com.kyberswap.android.domain.usecase.transaction.MonitorPendingTransactionUseCase
import com.kyberswap.android.domain.usecase.wallet.CheckEligibleWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetAllWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateWalletUseCase
import com.kyberswap.android.domain.usecase.walletconnect.WalletConnectKillSessionUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.GetPendingTransactionState
import com.kyberswap.android.presentation.main.balance.GetRatingInfoState
import com.kyberswap.android.presentation.main.balance.SaveRatingInfoState
import com.kyberswap.android.presentation.main.balance.SaveWalletState
import com.kyberswap.android.presentation.main.notification.GetUnReadNotificationsState
import com.kyberswap.android.presentation.main.notification.ReadNotificationsState
import com.kyberswap.android.presentation.main.profile.DataTransferState
import com.kyberswap.android.presentation.main.profile.LogoutState
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetMaxPriceState
import com.kyberswap.android.presentation.main.walletconnect.RequestState
import com.kyberswap.android.presentation.wallet.UpdateWalletState
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val getAllWalletUseCase: GetAllWalletUseCase,
    private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
    private val monitorPendingTransactionsUseCase: MonitorPendingTransactionUseCase,
    getWalletUseCase: GetSelectedWalletUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val updateSelectedWalletUseCase: UpdateSelectedWalletUseCase,
    private val getLoginStatusUseCase: GetUserInfoUseCase,
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val getOtherBalancePollingUseCase: GetOtherBalancePollingUseCase,
    private val getBatchTokenBalanceUseCase: GetTokensBalanceUseCase,
    private val forceBatchTokenBalanceUseCase: GetTokensBalanceUseCase,
    private val getTokenBalanceUseCase: GetTokenBalanceUseCase,
    private val getTransactionsPeriodicallyUseCase: GetTransactionsPeriodicallyUseCase,
    private val updateBalanceUseCase: UpdateBalanceUseCase,
    private val getRatingInfoUseCase: GetRatingUseCase,
    private val saveRatingInfoUseCase: SaveRatingInfoUseCase,
    private val readNotificationsUseCase: ReadNotificationsUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val notificationUseCase: GetUnReadNotificationUseCase,
    private val getUserDataPermissionUseCase: RefreshUserInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val dataTransferUseCase: DataTransferUseCase,
    private val getMaxGasPriceUseCase: GetMaxGasPriceUseCase,
    private val checkEligibleWalletUseCase: CheckEligibleWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase,
    private val killSessionUseCase: WalletConnectKillSessionUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getWalletUseCase, errorHandler) {

    private val _getAllWalletStateCallback = MutableLiveData<Event<GetAllWalletState>>()
    val getAllWalletStateCallback: LiveData<Event<GetAllWalletState>>
        get() = _getAllWalletStateCallback

    private val _getRatingInfoCallback = MutableLiveData<Event<GetRatingInfoState>>()
    val getRatingInfoCallback: LiveData<Event<GetRatingInfoState>>
        get() = _getRatingInfoCallback

    private val _checkEligibleWalletCallback = MutableLiveData<Event<CheckEligibleWalletState>>()
    val checkEligibleWalletCallback: LiveData<Event<CheckEligibleWalletState>>
        get() = _checkEligibleWalletCallback

    private val _saveRatingInfoCallback = MutableLiveData<Event<SaveRatingInfoState>>()
    val saveRatingInfoCallback: LiveData<Event<SaveRatingInfoState>>
        get() = _saveRatingInfoCallback

    private val _getMaxPriceCallback = MutableLiveData<Event<GetMaxPriceState>>()
    val getMaxPriceCallback: LiveData<Event<GetMaxPriceState>>
        get() = _getMaxPriceCallback

    private val _getPendingTransactionStateCallback =
        MutableLiveData<Event<GetPendingTransactionState>>()
    val getPendingTransactionStateCallback: LiveData<Event<GetPendingTransactionState>>
        get() = _getPendingTransactionStateCallback

    private val _getMnemonicCallback = MutableLiveData<Event<CreateWalletState>>()
    val createWalletCallback: LiveData<Event<CreateWalletState>>
        get() = _getMnemonicCallback

    private val _switchWalletCompleteCallback = MutableLiveData<Event<UpdateWalletState>>()
    val switchWalletCompleteCallback: LiveData<Event<UpdateWalletState>>
        get() = _switchWalletCompleteCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    private val _getDataTransferCallback = MutableLiveData<Event<UserInfoState>>()
    val getDataTransferCallback: LiveData<Event<UserInfoState>>
        get() = _getDataTransferCallback

    private val _logoutCallback = MutableLiveData<Event<LogoutState>>()
    val logoutCallback: LiveData<Event<LogoutState>>
        get() = _logoutCallback

    private var currentPendingList: List<Transaction> = listOf()

    private val hasTransaction: Boolean
        get() = _hasTransaction

    private var _hasTransaction: Boolean = false

    private var ratingInfo: RatingInfo? = null

    private val _readNotificationsCallback = MutableLiveData<Event<ReadNotificationsState>>()
    val readNotificationsCallback: LiveData<Event<ReadNotificationsState>>
        get() = _readNotificationsCallback

    private val _dataTransferCallback = MutableLiveData<Event<DataTransferState>>()
    val dataTransferCallback: LiveData<Event<DataTransferState>>
        get() = _dataTransferCallback

    private val _getNotificationsCallback = MutableLiveData<Event<GetUnReadNotificationsState>>()
    val getNotificationsCallback: LiveData<Event<GetUnReadNotificationsState>>
        get() = _getNotificationsCallback

    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
            },
            {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    private val _saveWalletCallback = MutableLiveData<Event<SaveWalletState>>()
    val saveWalletCallback: LiveData<Event<SaveWalletState>>
        get() = _saveWalletCallback


    fun saveWallet(wallet: Wallet) {
        updateWalletUseCase.dispose()
        updateWalletUseCase.execute(
            {
                _saveWalletCallback.value = Event(SaveWalletState.Success(""))
            },
            {
                it.printStackTrace()
                _saveWalletCallback.value =
                    Event(SaveWalletState.ShowError(it.localizedMessage))
            },
            wallet
        )
    }

    fun getWallets() {
        getAllWalletUseCase.execute(
            {
                _getAllWalletStateCallback.value = Event(
                    GetAllWalletState.Success(
                        it
                    )
                )
            },
            {
                it.printStackTrace()
                _getAllWalletStateCallback.value =
                    Event(
                        GetAllWalletState.ShowError(
                            errorHandler.getError(it)
                        )
                    )
            },
            null
        )
    }

    fun pollingTokenBalance(wallets: List<Wallet>, selectedWallet: Wallet) {
        monitorListedTokenBalance(wallets, selectedWallet)
        monitorOtherTokenBalance()
    }

    private fun monitorListedTokenBalance(wallets: List<Wallet>, selectedWallet: Wallet) {
        getBalancePollingUseCase.dispose()
        getBalancePollingUseCase.execute(
            {
                loadBalances(Pair(selectedWallet, it))
            },
            {
                it.printStackTrace()
            },
            GetBalancePollingUseCase.Param(wallets)
        )
    }

    fun monitorOtherTokenBalance() {
        getOtherBalancePollingUseCase.dispose()
        getOtherBalancePollingUseCase.execute(
            {
                loadOtherBalances(it)
            },
            {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
            },
            GetOtherBalancePollingUseCase.Param()

        )
    }

    fun checkEligibleWallet(wallet: Wallet) {
        checkEligibleWalletUseCase.dispose()
        checkEligibleWalletUseCase.execute(
            {

                _checkEligibleWalletCallback.value = Event(CheckEligibleWalletState.Success(it))
            },
            {
                _checkEligibleWalletCallback.value =
                    Event(CheckEligibleWalletState.ShowError(errorHandler.getError(it)))
            },
            CheckEligibleWalletUseCase.Param(wallet)
        )
    }

    fun getRatingInfo() {
        getRatingInfoUseCase.execute(
            {
                ratingInfo = it
                if ((it.isShowAlert || it.reShowAlert) && hasTransaction) {
                    _getRatingInfoCallback.value = Event(
                        GetRatingInfoState.Success(
                            it
                        )
                    )
                } else {
                    saveRatingInfo(it.copy(count = it.count + 1))
                }

            },
            {
                it.printStackTrace()
            },
            null
        )
    }

    private fun saveRatingInfo(ratingInfo: RatingInfo) {
        saveRatingInfoUseCase.execute(
            {

            },
            {
                it.printStackTrace()
            }, SaveRatingInfoUseCase.Param(ratingInfo)
        )
    }

    fun saveNotNow() {
        ratingInfo?.let {
            saveRatingInfo(
                it.copy(
                    isNotNow = true,
                    updatedAt = System.currentTimeMillis() / 1000L
                )
            )
        }
    }

    fun saveRatingFinish() {
        ratingInfo?.let {
            saveRatingInfo(
                it.copy(
                    isFinished = true
                )
            )
        }
    }

    private fun loadOtherBalances(others: List<Token>) {
        others.sortedByDescending { it.currentBalance }.forEach { token ->
            getTokenBalanceUseCase.execute(
                Action {

                },
                Consumer {
                    Timber.e(token.symbol)
                    Timber.e(it.localizedMessage)
                    it.printStackTrace()
                },
                token
            )
        }
    }

    fun getTransactionPeriodically(wallet: Wallet) {
        getTransactionsPeriodicallyUseCase.dispose()
        getTransactionsPeriodicallyUseCase.execute(
            Consumer {
                _hasTransaction = it.isNotEmpty()
            },
            Consumer {
                it.printStackTrace()
            },
            GetTransactionsPeriodicallyUseCase.Param(wallet)
        )
    }

    fun getPendingTransaction(wallet: Wallet) {
        getPendingTransactionsUseCase.dispose()
        getPendingTransactionsUseCase.execute(
            Consumer {

                if (it.isNotEmpty() && it != currentPendingList) {
                    currentPendingList = it
                    monitorPendingTransactionsUseCase.dispose()
                    monitorPendingTransactionsUseCase.execute(
                        Consumer { tx ->
                            _getPendingTransactionStateCallback.value = Event(
                                GetPendingTransactionState.Success(tx)
                            )

                            if (tx.none { it.blockNumber.isEmpty() }) {
                                monitorPendingTransactionsUseCase.dispose()
                            }
                        },
                        Consumer { ex ->
                            ex.printStackTrace()
                            Timber.e(ex.localizedMessage)
                        },
                        MonitorPendingTransactionUseCase.Param(it, wallet)
                    )
                } else {
                    if (it.isEmpty()) monitorPendingTransactionsUseCase.dispose()
                    _getPendingTransactionStateCallback.value = Event(
                        GetPendingTransactionState.Success(
                            it
                        )
                    )
                }

            },
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
                _getPendingTransactionStateCallback.value = Event(
                    GetPendingTransactionState.ShowError(
                        errorHandler.getError(it)
                    )
                )
            },
            wallet.address
        )
    }

    fun createWallet(walletName: String = "Untitled") {
        _getMnemonicCallback.postValue(Event(CreateWalletState.Loading))
        createWalletUseCase.execute(
            Consumer {
                _getMnemonicCallback.value =
                    Event(CreateWalletState.Success(it.first, it.second))
            },
            Consumer {
                it.printStackTrace()
                _getMnemonicCallback.value =
                    Event(CreateWalletState.ShowError(errorHandler.getError(it)))
            },
            CreateWalletUseCase.Param(walletName)
        )
    }

    private fun loadBalances(
        pair: Pair<Wallet, List<Token>>
    ) {
        getBatchTokenBalanceUseCase.dispose()
        getBatchTokenBalanceUseCase.execute(
            Action {
                updateBalance(pair.first)
            },
            Consumer {
                Timber.e(it.localizedMessage)
                it.printStackTrace()
            },
            GetTokensBalanceUseCase.Param(pair.first, pair.second)
        )
    }

    private fun updateBalance(wallet: Wallet) {
        updateBalanceUseCase.execute(
            Action {

            },
            Consumer {

            },
            UpdateBalanceUseCase.Param(wallet)
        )
    }

    private fun forceUpdateBalance(
        pair: Pair<Wallet, List<Token>>
    ) {
        forceBatchTokenBalanceUseCase.dispose()
        forceBatchTokenBalanceUseCase.execute(
            Action {
                _switchWalletCompleteCallback.value =
                    Event(UpdateWalletState.Success(pair.first, true))
                updateBalance(pair.first)
            },
            Consumer {
                Timber.e(it.localizedMessage)
                it.printStackTrace()
                _switchWalletCompleteCallback.value =
                    Event(UpdateWalletState.ShowError(errorHandler.getError(it)))
            },
            GetTokensBalanceUseCase.Param(pair.first, pair.second)
        )
    }

    fun updateSelectedWallet(wallet: Wallet) {
        updateSelectedWalletUseCase.dispose()
        _switchWalletCompleteCallback.postValue(Event(UpdateWalletState.Loading))
        updateSelectedWalletUseCase.execute(
            Consumer { wl ->
                forceUpdateBalance(wl)

            },
            Consumer {
                it.printStackTrace()
                _switchWalletCompleteCallback.value =
                    Event(UpdateWalletState.ShowError(errorHandler.getError(it)))
            },
            UpdateSelectedWalletUseCase.Param(wallet)
        )
    }

    fun updatePushToken(userId: String, token: String?) {
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

    fun getNotifications() {
        notificationUseCase.dispose()
        _getNotificationsCallback.postValue(Event(GetUnReadNotificationsState.Loading))
        notificationUseCase.execute(
            Consumer {
                _getNotificationsCallback.value =
                    Event(GetUnReadNotificationsState.Success(it))
            },
            Consumer {
                _getNotificationsCallback.value =
                    Event(GetUnReadNotificationsState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun readNotification(notification: Notification) {
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

    fun getMaxGasPrice() {
        getMaxGasPriceUseCase.dispose()
        getMaxGasPriceUseCase.execute(
            Consumer {
                if (it.success) {
                    _getMaxPriceCallback.value = Event(GetMaxPriceState.Success(it.data))
                }
            },
            Consumer {
                it.printStackTrace()
                _getMaxPriceCallback.value =
                    Event(GetMaxPriceState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun getDataTransferInfo() {
        getUserDataPermissionUseCase.dispose()
        getUserDataPermissionUseCase.execute(
            Consumer {
                _getDataTransferCallback.value = Event(UserInfoState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getDataTransferCallback.value =
                    Event(UserInfoState.ShowError(errorHandler.getError(it)))
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

    fun killSession() {
        killSessionUseCase.execute(
            {

            },
            {
                it.printStackTrace()
                Timber.e("error: %s", it.localizedMessage)

            },
            WalletConnectKillSessionUseCase.Param()
        )
    }

    override fun onCleared() {
        updateSelectedWalletUseCase.dispose()
        updateBalanceUseCase.dispose()
        getTokenBalanceUseCase.dispose()
        createWalletUseCase.dispose()
        forceBatchTokenBalanceUseCase.dispose()
        getAllWalletUseCase.dispose()
        getBalancePollingUseCase.dispose()
        getBatchTokenBalanceUseCase.dispose()
        getLoginStatusUseCase.dispose()
        getOtherBalancePollingUseCase.dispose()
        getRatingInfoUseCase.dispose()
        logoutUseCase.dispose()
        getTransactionsPeriodicallyUseCase.dispose()
        getUserDataPermissionUseCase.dispose()
        notificationUseCase.dispose()
        readNotificationsUseCase.dispose()
        monitorPendingTransactionsUseCase.dispose()
        saveRatingInfoUseCase.dispose()
        getPendingTransactionsUseCase.dispose()
        updatePushTokenUseCase.dispose()
        checkEligibleWalletUseCase.dispose()
        super.onCleared()
    }

    fun transfer(action: String, userInfo: UserInfo) {
        dataTransferUseCase.execute(
            Consumer {
                _dataTransferCallback.value =
                    Event(DataTransferState.Success(it, userInfo.copy(transferPermission = action)))
            },
            Consumer {
                _dataTransferCallback.value =
                    Event(
                        DataTransferState.ShowError(
                            errorHandler.getError(it),
                            userInfo.copy(transferPermission = action)
                        )
                    )
                it.printStackTrace()
            },
            DataTransferUseCase.Param(action)
        )
    }
}