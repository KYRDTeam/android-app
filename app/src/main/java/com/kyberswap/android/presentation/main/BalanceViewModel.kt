package com.kyberswap.android.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.usecase.token.GetBalanceUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class BalanceViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase
) : ViewModel() {

    private val _getBalanceStateCallback = MutableLiveData<Event<GetBalanceState>>()
    val getBalanceStateCallback: LiveData<Event<GetBalanceState>>
        get() = _getBalanceStateCallback

    fun getTokenBalance(address: String) {
        _getBalanceStateCallback.postValue(Event(GetBalanceState.Loading))
        getBalanceUseCase.execute(
            Consumer {

                _getBalanceStateCallback.value = Event(GetBalanceState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getBalanceStateCallback.value =
                    Event(GetBalanceState.ShowError(it.localizedMessage))
    ,
            GetBalanceUseCase.Param(address)
        )
    }
}