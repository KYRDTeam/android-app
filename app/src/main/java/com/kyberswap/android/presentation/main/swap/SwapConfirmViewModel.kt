package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.swap.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SwapConfirmViewModel @Inject constructor(
    private val getSwapData: GetSwapDataUseCase,
    private val swapTokenUseCase: SwapTokenUseCase
) : ViewModel() {

    private val _getSwapCallback = MutableLiveData<Event<GetSwapState>>()
    val getSwapDataCallback: LiveData<Event<GetSwapState>>
        get() = _getSwapCallback


    private val _swapTokenTransactionCallback =
        MutableLiveData<Event<SwapTokenTransactionState>>()
    val swapTokenTransactionCallback: LiveData<Event<SwapTokenTransactionState>>
        get() = _swapTokenTransactionCallback


    fun getSwapData(wallet: Wallet) {
        getSwapData.execute(
            Consumer {
                _getSwapCallback.value = Event(GetSwapState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getSwapCallback.value = Event(GetSwapState.ShowError(it.localizedMessage))
    ,
            GetSwapDataUseCase.Param(wallet)
        )
    }

    fun swap(wallet: Wallet?, swap: Swap?) {
        swap?.let {
            _swapTokenTransactionCallback.postValue(Event(SwapTokenTransactionState.Loading))
            swapTokenUseCase.execute(
                Consumer {
                    _swapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.Success(it))
        ,
                Consumer {
                    it.printStackTrace()
                    _swapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.ShowError(it.localizedMessage))
        ,
                SwapTokenUseCase.Param(wallet!!, swap)

            )

    }

}