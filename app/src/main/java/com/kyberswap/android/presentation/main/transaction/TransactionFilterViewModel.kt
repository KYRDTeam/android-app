package com.kyberswap.android.presentation.main.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetTokenUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.balance.GetBalanceState
import io.reactivex.functions.Consumer
import javax.inject.Inject

class TransactionFilterViewModel @Inject constructor(
    private val getTokenListUseCase: GetTokenUseCase
) : ViewModel() {
    private val _getTokenListCallback = MutableLiveData<Event<GetBalanceState>>()
    val getTokenListCallback: LiveData<Event<GetBalanceState>>
        get() = _getTokenListCallback

    fun getTokenList(address: String) {
        getTokenListUseCase.execute(
            Consumer {
                _getTokenListCallback.value = Event(
                    GetBalanceState.Success(
                        it
                    )
                )
    ,
            Consumer {
                it.printStackTrace()
                _getTokenListCallback.value =
                    Event(
                        GetBalanceState.ShowError(
                            it.localizedMessage
                        )
                    )
    ,
            address
        )
    }
}