package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.transaction.GetTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ext.toDate
import com.kyberswap.android.util.ext.toLongSafe
import io.reactivex.functions.Consumer
import javax.inject.Inject

class TransactionStatusViewModel @Inject constructor(
    private val getTransactionFilterUseCase: GetTransactionFilterUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _getTransactionCallback = MutableLiveData<Event<GetTransactionState>>()
    val getTransactionCallback: LiveData<Event<GetTransactionState>>
        get() = _getTransactionCallback


    private fun getTransaction(type: Int, wallet: Wallet, transactionFilter: TransactionFilter) {
        getTransactionsUseCase.dispose()
        _getTransactionCallback.postValue(Event(GetTransactionState.Loading))
        getTransactionsUseCase.execute(
            Consumer { transactions ->
                val itemList = transactions
                    .filter {
                        (transactionFilter.from.isEmpty() || it.timeStamp.toLongSafe() * 1000 >= transactionFilter.from.toDate().time) &&
                            (transactionFilter.to.isEmpty() || it.timeStamp.toLongSafe() * 1000 <= transactionFilter.to.toDate().time) &&
                            transactionFilter.types.contains(it.type) &&
                            transactionFilter.tokens.contains(it.tokenSymbol)
                    }
                    .groupBy { it.shortedDateTimeFormat }
                    .flatMap { item ->
                        val items = mutableListOf<TransactionItem>()
                        items.add(TransactionItem.Header(item.key))
                        val list = item.value.sortedByDescending { it.timeStamp.toLong() }
                        list.forEachIndexed { index, transaction ->
                            if (index % 2 == 0) {
                                items.add(TransactionItem.ItemEven(transaction))
                            } else {
                                items.add(TransactionItem.ItemOdd(transaction))
                            }
                        }
                        items
                    }
                _getTransactionCallback.value = Event(GetTransactionState.Success(itemList))
            },
            Consumer {
                it.printStackTrace()
                _getTransactionCallback.value =
                    Event(GetTransactionState.ShowError(it.localizedMessage))
            },
            GetTransactionsUseCase.Param(type, wallet)
        )
    }

    fun getTransactionFilter(type: Int, wallet: Wallet) {
        getTransactionFilterUseCase.execute(
            Consumer {
                getTransaction(type, wallet, it)
            },
            Consumer {

            },
            GetTransactionFilterUseCase.Param(wallet.address)
        )
    }

    public override fun onCleared() {
        getTransactionsUseCase.dispose()
        super.onCleared()
    }

}