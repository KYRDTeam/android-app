package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.wallet.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.wallet.SwapTokenUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

class SwapConfirmViewModel @Inject constructor(
    private val getSwapData: GetSwapDataUseCase,
    private val swapTokenUseCase: SwapTokenUseCase
) : ViewModel() {

    private val _getSwapCallback = MutableLiveData<Event<GetSwapState>>()
    val getSwapDataCallback: LiveData<Event<GetSwapState>>
        get() = _getSwapCallback


    private val _getSwapTokenTransactionCallback =
        MutableLiveData<Event<SwapTokenTransactionState>>()
    val getSwapTokenTransactionCallback: LiveData<Event<SwapTokenTransactionState>>
        get() = _getSwapTokenTransactionCallback


    fun getSwapData(address: String) {
        getSwapData.execute(
            Consumer {
                _getSwapCallback.value = Event(GetSwapState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getSwapCallback.value = Event(GetSwapState.ShowError(it.localizedMessage))
    ,
            GetSwapDataUseCase.Param(address)
        )
    }

    fun swap(wallet: Wallet?, swap: Swap?) {
        swap?.let {
            _getSwapTokenTransactionCallback.postValue(Event(SwapTokenTransactionState.Loading))
            swapTokenUseCase.execute(
                Consumer {
                    _getSwapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.Success(it))
                    Timber.e("txhash: " + it)
        ,
                Consumer {
                    it.printStackTrace()
                    _getSwapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.ShowError(it.localizedMessage))
        ,
                SwapTokenUseCase.Param(wallet!!, swap)

            )

    }

}