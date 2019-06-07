package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrdersUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ManageOrderViewModel @Inject constructor(
    private val getLimitOrdersUseCase: GetLimitOrdersUseCase
) : ViewModel() {

    private val _getRelatedOrderCallback = MutableLiveData<Event<GetRelatedOrdersState>>()
    val getRelatedOrderCallback: LiveData<Event<GetRelatedOrdersState>>
        get() = _getRelatedOrderCallback


    fun getRelatedOrders(wallet: Wallet) {
        getLimitOrdersUseCase.execute(
            Consumer {
                _getRelatedOrderCallback.value = Event(GetRelatedOrdersState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.ShowError(it.localizedMessage))

    ,
            GetLimitOrdersUseCase.Param(wallet.address)
        )

    }
}