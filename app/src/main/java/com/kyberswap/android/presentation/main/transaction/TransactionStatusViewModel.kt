package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.transaction.GetPendingTransactionsUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.util.ErrorHandler
import com.kyberswap.android.util.ext.toDate
import io.reactivex.functions.Consumer
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class TransactionStatusViewModel @Inject constructor(
    private val getTransactionFilterUseCase: GetTransactionFilterUseCase,
    private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _getTransactionCallback = MutableLiveData<Event<GetTransactionState>>()
    val getTransactionCallback: LiveData<Event<GetTransactionState>>
        get() = _getTransactionCallback

    private var currentFilter: TransactionFilter? = null
    var transactionList = listOf<Transaction>()

    private fun getTransaction(
        type: Int,
        wallet: Wallet,
        transactionFilter: TransactionFilter,
        isForceRefresh: Boolean
    ) {
        if (type == Transaction.PENDING) {
            if (isFilterChanged(transactionFilter) || isForceRefresh) {
                getPendingTransactions(wallet, transactionFilter)
            } else {
                _getTransactionCallback.value =
                    Event(GetTransactionState.FilterNotChange(true))
            }
        } else {
            if (isFilterChanged(transactionFilter) || isForceRefresh) {
                getMinedTransactions(wallet, transactionFilter, isForceRefresh)
            } else {
                _getTransactionCallback.value =
                    Event(GetTransactionState.FilterNotChange(true))
            }
        }
    }

    private fun isFilterChanged(transactionFilter: TransactionFilter): Boolean {
        return currentFilter != transactionFilter
    }

    private fun getMinedTransactions(
        wallet: Wallet,
        transactionFilter: TransactionFilter,
        isForceRefresh: Boolean
    ) {
        getTransactionsUseCase.dispose()
        _getTransactionCallback.postValue(Event(GetTransactionState.Loading))
        getTransactionsUseCase.execute(
            Consumer { response ->
                _getTransactionCallback.value = Event(
                    GetTransactionState.Success(
                        filterTransaction(
                            response.transactionList,
                            transactionFilter
                        ),
                        currentFilter != transactionFilter,
                        response.isLoaded

                    )
                )
                transactionList = response.transactionList
                currentFilter = transactionFilter

            },
            Consumer {
                Timber.e(it.localizedMessage)
                _getTransactionCallback.value =
                    Event(GetTransactionState.ShowError(errorHandler.getError(it)))
            },
            GetTransactionsUseCase.Param(wallet, isForceRefresh)
        )
    }

    private fun getPendingTransactions(
        wallet: Wallet,
        transactionFilter: TransactionFilter
    ) {
        getPendingTransactionsUseCase.dispose()
        _getTransactionCallback.postValue(Event(GetTransactionState.Loading))
        getPendingTransactionsUseCase.execute(
            Consumer {
                _getTransactionCallback.value = Event(
                    GetTransactionState.Success(
                        filterTransaction(
                            it,
                            transactionFilter
                        ),
                        currentFilter != transactionFilter,
                        true
                    )
                )
                transactionList = it
                currentFilter = transactionFilter

            },
            Consumer {
                Timber.e(it.localizedMessage)
                _getTransactionCallback.value =
                    Event(GetTransactionState.ShowError(errorHandler.getError(it)))
            },
            wallet.address
        )
    }

    private fun filterTransaction(
        transactions: List<Transaction>,
        transactionFilter: TransactionFilter
    ): List<TransactionItem> {

        return transactions
            .sortedByDescending { it.timeStamp }
            .filter {
                val tokenList = transactionFilter.tokens.map { it.toLowerCase(Locale.getDefault()) }
                (transactionFilter.from.isEmpty() || it.filterDateTimeFormat.toDate().time >= transactionFilter.from.toDate().time) &&
                    (transactionFilter.to.isEmpty() || it.filterDateTimeFormat.toDate().time <= transactionFilter.to.toDate().time) &&
                    transactionFilter.types.contains(it.type) &&
                    (tokenList.contains(it.tokenSymbol.toLowerCase(Locale.getDefault())) ||
                        tokenList.contains(it.tokenSource.toLowerCase(Locale.getDefault()))
                        || tokenList.contains(it.tokenDest.toLowerCase(Locale.getDefault()))
                        )
            }
            .groupBy { it.shortedDateTimeFormat }
            .flatMap { item ->
                val items = mutableListOf<TransactionItem>()
                items.add(TransactionItem.Header(item.key))
                val list = item.value.sortedByDescending { it.timeStamp }
                list.forEachIndexed { index, transaction ->
                    if (index % 2 == 0) {
                        items.add(TransactionItem.ItemEven(transaction))
                    } else {
                        items.add(TransactionItem.ItemOdd(transaction))
                    }
                }
                items
            }
    }

    fun getTransactionFilter(type: Int, wallet: Wallet, isForceRefresh: Boolean) {
        getTransactionFilterUseCase.dispose()
        getTransactionFilterUseCase.execute(
            Consumer {
                getTransaction(type, wallet, it, isForceRefresh)
            },
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
                _getTransactionCallback.value =
                    Event(GetTransactionState.ShowError(errorHandler.getError(it)))
            },
            GetTransactionFilterUseCase.Param(wallet.address)
        )
    }

    public override fun onCleared() {
        getTransactionsUseCase.dispose()
        getPendingTransactionsUseCase.dispose()
        getTransactionFilterUseCase.dispose()
        super.onCleared()
    }
}