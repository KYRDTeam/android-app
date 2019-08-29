package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.FilterItem
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.usecase.token.GetTokenListUseCase
import com.kyberswap.android.domain.usecase.transaction.GetTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.transaction.SaveTransactionFilterUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class TransactionFilterViewModel @Inject constructor(
    private val getTokenListUseCase: GetTokenListUseCase,
    private val getTransactionFilterUseCase: GetTransactionFilterUseCase,
    private val saveTransactionFilterUseCase: SaveTransactionFilterUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {
    private val _getTransactionFilterCallback = MutableLiveData<Event<GetTransactionFilterState>>()
    val getTransactionFilterCallback: LiveData<Event<GetTransactionFilterState>>
        get() = _getTransactionFilterCallback

    private val _saveTransactionFilterCallback =
        MutableLiveData<Event<SaveTransactionFilterState>>()
    val saveTransactionFilterCallback: LiveData<Event<SaveTransactionFilterState>>
        get() = _saveTransactionFilterCallback

    private fun getTokenList(address: String, transactionFilter: TransactionFilter) {
        getTokenListUseCase.execute(
            Consumer {
                val items = it.map { token ->
                    FilterItem(transactionFilter.tokens.find {
                        it == token.tokenSymbol
                    } != null, token.tokenSymbol)
                }
                _getTransactionFilterCallback.value = Event(
                    GetTransactionFilterState.Success(
                        transactionFilter, items
                    )
                )
            },
            Consumer {
                it.printStackTrace()
                _getTransactionFilterCallback.value =
                    Event(
                        GetTransactionFilterState.ShowError(
                            errorHandler.getError(it)
                        )
                    )
            },
            address
        )
    }

    fun getTransactionFilter(address: String) {
        getTransactionFilterUseCase.execute(
            Consumer {
                getTokenList(address, it)
            },
            Consumer {

            },
            GetTransactionFilterUseCase.Param(address)
        )
    }

    fun saveTransactionFilter(transactionFilter: TransactionFilter) {
        saveTransactionFilterUseCase.execute(
            Action {
                _saveTransactionFilterCallback.value = Event(SaveTransactionFilterState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveTransactionFilterCallback.value =
                    Event(SaveTransactionFilterState.ShowError(errorHandler.getError(it)))
            },
            SaveTransactionFilterUseCase.Param(transactionFilter)
        )
    }
}