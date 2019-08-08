package com.kyberswap.android.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.transaction.GetPendingTransactionsUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsPeriodicallyUseCase
import com.kyberswap.android.domain.usecase.transaction.MonitorPendingTransactionUseCase
import com.kyberswap.android.domain.usecase.wallet.CreateWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetAllWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.UpdateSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.landing.CreateWalletState
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.GetPendingTransactionState
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.splash.GetWalletState
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
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val getTransactionsPeriodicallyUseCase: GetTransactionsPeriodicallyUseCase
) : SelectedWalletViewModel(getWalletUseCase) {

    private val _getAllWalletStateCallback = MutableLiveData<Event<GetAllWalletState>>()
    val getAllWalletStateCallback: LiveData<Event<GetAllWalletState>>
        get() = _getAllWalletStateCallback

    private val _getPendingTransactionStateCallback =
        MutableLiveData<Event<GetPendingTransactionState>>()
    val getPendingTransactionStateCallback: LiveData<Event<GetPendingTransactionState>>
        get() = _getPendingTransactionStateCallback

    private val _getMnemonicCallback = MutableLiveData<Event<CreateWalletState>>()
    val createWalletCallback: LiveData<Event<CreateWalletState>>
        get() = _getMnemonicCallback

    private val _getWalletStateCallback = MutableLiveData<Event<GetWalletState>>()
    val getWalletStateCallback: LiveData<Event<GetWalletState>>
        get() = _getWalletStateCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
    ,
            null
        )
    }


    fun getWallets() {
        getAllWalletUseCase.execute(
            Consumer {
                _getAllWalletStateCallback.value = Event(
                    GetAllWalletState.Success(
                        it
                    )
                )
    ,
            Consumer {
                it.printStackTrace()
                _getAllWalletStateCallback.value =
                    Event(
                        GetAllWalletState.ShowError(
                            it.localizedMessage
                        )
                    )
    ,
            null
        )
    }

    fun pollingTokenBalance(wallets: List<Wallet>) {
        getBalancePollingUseCase.dispose()
        getBalancePollingUseCase.execute(
            Consumer {

    ,
            Consumer {
                it.printStackTrace()
    ,
            GetBalancePollingUseCase.Param(wallets)
        )

    }

    fun getTransactionPeriodically(wallet: Wallet) {
        getTransactionsPeriodicallyUseCase.dispose()
        getTransactionsPeriodicallyUseCase.execute(
            Consumer { },
            Consumer {
                it.printStackTrace()
    ,
            GetTransactionsPeriodicallyUseCase.Param(wallet)
        )
    }

    fun getPendingTransaction(wallet: Wallet) {
        getPendingTransactionsUseCase.dispose()
        getPendingTransactionsUseCase.execute(
            Consumer {
                if (it.isNotEmpty()) {
                    monitorPendingTransactionsUseCase.dispose()
                    monitorPendingTransactionsUseCase.execute(
                        Consumer { tx ->

                            _getPendingTransactionStateCallback.value = Event(
                                GetPendingTransactionState.Success(tx)
                            )

                            if (tx.none { it.blockNumber.isEmpty() }) {
                                monitorPendingTransactionsUseCase.dispose()
                    
                ,
                        Consumer { ex ->
                            ex.printStackTrace()
                            Timber.e(ex.localizedMessage)
                ,
                        MonitorPendingTransactionUseCase.Param(it, wallet)
                    )
         else {
                    _getPendingTransactionStateCallback.value = Event(
                        GetPendingTransactionState.Success(
                            it
                        )
                    )
        

    ,
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
                _getPendingTransactionStateCallback.value = Event(
                    GetPendingTransactionState.ShowError(
                        it.localizedMessage
                    )
                )
    ,
            wallet.address
        )
    }

    fun createWallet(walletName: String = "Untitled") {
        _getMnemonicCallback.postValue(Event(CreateWalletState.Loading))
        createWalletUseCase.execute(
            Consumer {
                _getMnemonicCallback.value =
                    Event(CreateWalletState.Success(it.first, it.second))
    ,
            Consumer {
                it.printStackTrace()
                _getMnemonicCallback.value =
                    Event(CreateWalletState.ShowError(it.localizedMessage))
    ,
            CreateWalletUseCase.Param(walletName)
        )
    }

    fun updateSelectedWallet(wallet: Wallet) {
        getBalancePollingUseCase.dispose()
        updateSelectedWalletUseCase.execute(
            Consumer { wl ->
                _getWalletStateCallback.value = Event(GetWalletState.Success(wl))

    ,
            Consumer {
                it.printStackTrace()
                _getWalletStateCallback.value =
                    Event(GetWalletState.ShowError(it.localizedMessage))
    ,
            UpdateSelectedWalletUseCase.Param(wallet)
        )
    }

}