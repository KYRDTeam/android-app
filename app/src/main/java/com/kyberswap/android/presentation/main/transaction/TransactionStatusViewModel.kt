package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class TransactionStatusViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _getTransactionCallback = MutableLiveData<Event<GetTransactionState>>()
    val getTransactionCallback: LiveData<Event<GetTransactionState>>
        get() = _getTransactionCallback


    fun getTransaction(address: String) {
        _getTransactionCallback.postValue(Event(GetTransactionState.Loading))
        getTransactionsUseCase.execute(
            Consumer { transactions ->
                val itemList = transactions.groupBy { it.shortedDateTimeFormat }
                    .flatMap { item ->
                        val items = mutableListOf<TransactionItem>()
                        items.add(TransactionItem.Header(item.key))
                        val list = item.value.sortedByDescending { it.timeStamp.toLong() }
                        list.forEachIndexed { index, transaction ->
                            if (index % 2 == 0) {
                                items.add(TransactionItem.ItemEven(transaction))
                     else {
                                items.add(TransactionItem.ItemOdd(transaction))
                    
                
                        items
            
                _getTransactionCallback.value = Event(GetTransactionState.Success(itemList))
    ,
            Consumer {
                it.printStackTrace()
                _getTransactionCallback.value =
                    Event(GetTransactionState.ShowError(it.localizedMessage))
    ,
            address
        )
    }

    public override fun onCleared() {
        getTransactionsUseCase.dispose()
        super.onCleared()
    }

}