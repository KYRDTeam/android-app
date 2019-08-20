package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.transaction.GetPendingTransactionsUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ext.toDate
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class TransactionStatusViewModel @Inject constructor(
    private val getTransactionFilterUseCase: GetTransactionFilterUseCase,
    private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _getTransactionCallback = MutableLiveData<Event<GetTransactionState>>()
    val getTransactionCallback: LiveData<Event<GetTransactionState>>
        get() = _getTransactionCallback

    private val _showProgressCallback = MutableLiveData<Event<ShowRefreshState>>()
    val showProgressCallback: LiveData<Event<ShowRefreshState>>
        get() = _showProgressCallback

    private var currentFilter: TransactionFilter? = null


    private fun getTransaction(type: Int, wallet: Wallet, transactionFilter: TransactionFilter) {
        if (type == Transaction.PENDING) {
            getPendingTransactionsUseCase.dispose()
            getPendingTransactionsUseCase.execute(
                Consumer {
                    _getTransactionCallback.value = Event(
                        GetTransactionState.Success(
                            filterTransaction(
                                it,
                                transactionFilter
                            ),
                            currentFilter != transactionFilter
                        )
                    )

                    _showProgressCallback.value = Event(ShowRefreshState.Success(true))
                },
                Consumer {
                    Timber.e(it.localizedMessage)
                    _getTransactionCallback.value =
                        Event(GetTransactionState.ShowError(it.localizedMessage))
                    _showProgressCallback.value = Event(ShowRefreshState.Success(true))
                },
                wallet.address
            )
        } else {
            getTransactionsUseCase.dispose()
            getTransactionsUseCase.execute(
                Consumer { response ->
                    _getTransactionCallback.value = Event(
                        GetTransactionState.Success(
                            filterTransaction(
                                response.transactionList,
                                transactionFilter
                            ),
                            currentFilter != transactionFilter

                        )
                    )

                    _showProgressCallback.value = Event(ShowRefreshState.Success(response.isLoaded))
                },
                Consumer {
                    Timber.e(it.localizedMessage)
                    _getTransactionCallback.value =
                        Event(GetTransactionState.ShowError(it.localizedMessage))
                    _showProgressCallback.value = Event(ShowRefreshState.Success(true))
                },
                GetTransactionsUseCase.Param(wallet)
            )
        }
    }

    private fun filterTransaction(
        transactions: List<Transaction>,
        transactionFilter: TransactionFilter
    ): List<TransactionItem> {
        return transactions
            .sortedByDescending { it.timeStamp }
            .filter {
                val tokenList = transactionFilter.tokens.map { it.toLowerCase() }
                (transactionFilter.from.isEmpty() || it.timeStamp * 1000 >= transactionFilter.from.toDate().time) &&
                    (transactionFilter.to.isEmpty() || it.timeStamp * 1000 <= transactionFilter.to.toDate().time) &&
                    transactionFilter.types.contains(it.type) &&
                    (tokenList.contains(it.tokenSymbol.toLowerCase()) ||
                        tokenList.contains(it.tokenSource.toLowerCase())
                        || tokenList.contains(it.tokenDest.toLowerCase()))
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

    fun getTransactionFilter(type: Int, wallet: Wallet, isForceRefresh: Boolean = false) {
        getTransactionFilterUseCase.dispose()
        getTransactionFilterUseCase.execute(
            Consumer {
                if (currentFilter != it || isForceRefresh) {
                    currentFilter = it
                    getTransaction(type, wallet, it)
                } else {
                    _showProgressCallback.value = Event(ShowRefreshState.Success(true))
                }

            },
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
                _getTransactionCallback.value =
                    Event(GetTransactionState.ShowError(it.localizedMessage))
                _showProgressCallback.value = Event(ShowRefreshState.Success(true))
            },
            GetTransactionFilterUseCase.Param(wallet.address)
        )
    }

    public override fun onCleared() {
        getTransactionsUseCase.dispose()
        super.onCleared()
    }
}