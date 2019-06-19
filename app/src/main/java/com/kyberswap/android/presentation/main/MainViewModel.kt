package com.kyberswap.android.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.transaction.GetPendingTransactionsUseCase
import com.kyberswap.android.domain.usecase.transaction.MonitorPendingTransactionUseCase
import com.kyberswap.android.domain.usecase.wallet.GetAllWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.GetAllWalletState
import com.kyberswap.android.presentation.main.balance.GetPendingTransactionState
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val getAllWalletUseCase: GetAllWalletUseCase,
    private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
    private val monitorPendingTransactionsUseCase: MonitorPendingTransactionUseCase
) : ViewModel() {

    private val _getAllWalletStateCallback = MutableLiveData<Event<GetAllWalletState>>()
    val getAllWalletStateCallback: LiveData<Event<GetAllWalletState>>
        get() = _getAllWalletStateCallback

    private val _getPendingTransactionStateCallback =
        MutableLiveData<Event<GetPendingTransactionState>>()
    val getPendingTransactionStateCallback: LiveData<Event<GetPendingTransactionState>>
        get() = _getPendingTransactionStateCallback

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

    fun getPendingTransaction(wallet: Wallet) {
        getPendingTransactionsUseCase.execute(
            Consumer {
                monitorPendingTransactionsUseCase.dispose()
                if (it.isNotEmpty()) {
                    monitorPendingTransactionsUseCase.execute(
                        Consumer { tx ->
                            _getPendingTransactionStateCallback.value = Event(
                                GetPendingTransactionState.Success(
                                    tx
                                )
                            )

                ,
                        Consumer { ex ->
                            ex.printStackTrace()
                            Timber.e(ex.localizedMessage)
                ,
                        MonitorPendingTransactionUseCase.Param(it)
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

}