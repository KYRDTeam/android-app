package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.*
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.util.ext.display
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigDecimal
import javax.inject.Inject

class LimitOrderViewModel @Inject constructor(
    private val getRelatedLimitOrdersUseCase: GetRelatedLimitOrdersUseCase,
    private val getLocalLimitOrderDataUseCase: GetLocalLimitOrderDataUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val getExpectedRateUseCase: GetExpectedRateUseCase,
    private val getMarketRate: GetMarketRateUseCase,
    private val saveLimitOrderUseCase: SaveLimitOrderUseCase,
    private val getLimitOrderFee: GetLimitOrderFeeUseCase,
    private val submitOrderUseCase: SubmitOrderUseCase,
    private val getNonceUseCase: GetNonceUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase
) : ViewModel() {

    private val _cancelOrderCallback = MutableLiveData<Event<CancelOrdersState>>()
    val cancelOrderCallback: LiveData<Event<CancelOrdersState>>
        get() = _cancelOrderCallback

    private val _getLocalLimitOrderCallback = MutableLiveData<Event<GetLocalLimitOrderState>>()
    val getLocalLimitOrderCallback: LiveData<Event<GetLocalLimitOrderState>>
        get() = _getLocalLimitOrderCallback

    private val _submitOrderCallback = MutableLiveData<Event<SubmitOrderState>>()
    val submitOrderCallback: LiveData<Event<SubmitOrderState>>
        get() = _submitOrderCallback


    private val _getGetNonceStateCallback = MutableLiveData<Event<GetNonceState>>()
    val getGetNonceStateCallback: LiveData<Event<GetNonceState>>
        get() = _getGetNonceStateCallback

    private val _getRelatedOrderCallback = MutableLiveData<Event<GetRelatedOrdersState>>()
    val getRelatedOrderCallback: LiveData<Event<GetRelatedOrdersState>>
        get() = _getRelatedOrderCallback


    private val _getExpectedRateCallback = MutableLiveData<Event<GetExpectedRateState>>()
    val getExpectedRateCallback: LiveData<Event<GetExpectedRateState>>
        get() = _getExpectedRateCallback

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val _getGetMarketRateCallback = MutableLiveData<Event<GetMarketRateState>>()
    val getGetMarketRateCallback: LiveData<Event<GetMarketRateState>>
        get() = _getGetMarketRateCallback


    private val _getFeeCallback = MutableLiveData<Event<GetFeeState>>()
    val getFeeCallback: LiveData<Event<GetFeeState>>
        get() = _getFeeCallback

    private val _saveOrderCallback = MutableLiveData<Event<SaveLimitOrderState>>()
    val saveOrderCallback: LiveData<Event<SaveLimitOrderState>>
        get() = _saveOrderCallback

    fun getLimitOrders(wallet: Wallet?) {
        wallet?.let {
            getLocalLimitOrderDataUseCase.execute(
                Consumer {
                    _getLocalLimitOrderCallback.value = Event(GetLocalLimitOrderState.Success(it))
                    getRelatedOrders(it, wallet)
                    getNonce(it, wallet)
                },
                Consumer {
                    it.printStackTrace()
                    _getLocalLimitOrderCallback.value =
                        Event(GetLocalLimitOrderState.ShowError(it.localizedMessage))
                },
                GetLocalLimitOrderDataUseCase.Param(wallet.address)
            )
        }

    }

    fun cancelOrder(order: Order) {
        _cancelOrderCallback.postValue(Event(CancelOrdersState.Loading))
        cancelOrderUseCase.execute(
            Consumer {
                _cancelOrderCallback.value = Event(CancelOrdersState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _cancelOrderCallback.value =
                    Event(CancelOrdersState.ShowError(it.localizedMessage))
            },
            CancelOrderUseCase.Param(order)
        )
    }

    fun getNonce(order: LocalLimitOrder, wallet: Wallet) {
        getNonceUseCase.execute(
            Consumer {
                _getGetNonceStateCallback.value = Event(GetNonceState.Success(it))

            },
            Consumer {
                it.printStackTrace()
                _getGetNonceStateCallback.value =
                    Event(GetNonceState.ShowError(it.localizedMessage))
            },
            GetNonceUseCase.Param(order, wallet)
        )
    }


    fun getRelatedOrders(order: LocalLimitOrder, wallet: Wallet) {
        getRelatedLimitOrdersUseCase.execute(
            Consumer {
                _getRelatedOrderCallback.value = Event(GetRelatedOrdersState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.ShowError(it.localizedMessage))

            },
            GetRelatedLimitOrdersUseCase.Param(
                wallet.address,
                order.tokenSource, order.tokenDest
            )
        )

    }

    fun getMarketRate(order: LocalLimitOrder) {

        if (order.hasSamePair) {
            _getGetMarketRateCallback.value =
                Event(GetMarketRateState.Success(BigDecimal.ONE.toDisplayNumber()))
            return
        }
        getMarketRate.dispose()
        if (order.hasSamePair) {
            getMarketRate.execute(
                Consumer {
                    _getGetMarketRateCallback.value = Event(GetMarketRateState.Success(it))
                },
                Consumer {
                    it.printStackTrace()
                    _getGetMarketRateCallback.value =
                        Event(GetMarketRateState.ShowError(it.localizedMessage))
                },
                GetMarketRateUseCase.Param(
                    order.tokenSource.tokenSymbol,
                    order.tokenDest.tokenSymbol
                )
            )
        }
    }

    fun getExpectedRate(
        order: LocalLimitOrder,
        srcAmount: String
    ) {
        if (order.hasSamePair) {
            _getExpectedRateCallback.value =
                Event(GetExpectedRateState.Success(listOf(BigDecimal.ONE.toDisplayNumber())))
            return
        }

        getExpectedRateUseCase.dispose()
        getExpectedRateUseCase.execute(
            Consumer {
                if (it.isNotEmpty()) {
                    _getExpectedRateCallback.value = Event(GetExpectedRateState.Success(it))
                }
                _getExpectedRateCallback.value = Event(GetExpectedRateState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getExpectedRateCallback.value =
                    Event(GetExpectedRateState.ShowError(it.localizedMessage))
            },
            GetExpectedRateUseCase.Param(
                order.userAddr,
                order.tokenSource,
                order.tokenDest,
                srcAmount
            )
        )
    }

    override fun onCleared() {
        getWalletByAddressUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }


    fun saveLimitOrder(order: LocalLimitOrder, fromContinue: Boolean = false) {

        saveLimitOrderUseCase.execute(
            Action {
                if (fromContinue) {
                    _saveOrderCallback.value = Event(SaveLimitOrderState.Success(""))
                }
            },
            Consumer {
                it.printStackTrace()
                _saveOrderCallback.value = Event(SaveLimitOrderState.ShowError(it.localizedMessage))
            },
            SaveLimitOrderUseCase.Param(order)
        )
    }

    fun getFee(
        order: LocalLimitOrder?,
        sourceAmount: String?,
        destAmount: String?,
        wallet: Wallet?
    ) {
        if (order == null || wallet == null) return
        getLimitOrderFee.execute(
            Consumer {
                _getFeeCallback.value = Event(GetFeeState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getFeeCallback.value = Event(GetFeeState.ShowError(it.localizedMessage))

            },
            GetLimitOrderFeeUseCase.Param(
                order.tokenSource,
                order.tokenDest,
                sourceAmount ?: 0.toString(),
                destAmount ?: 0.toString(),
                wallet.address
            )
        )
    }

    fun validate(order: LocalLimitOrder?, pendingOrders: List<Order>): Boolean {
        if (order == null) return false
        pendingOrders.forEach {
            if (order.minRate < it.minRate) {
                return false
            }
        }
        return true
    }

    private fun validateBalance(order: LocalLimitOrder, pendingOrders: List<Order>): Boolean {
        order.tokenSource
        return false
    }

    fun submitOrder(order: LocalLimitOrder?, wallet: Wallet?) {
        if (order == null || wallet == null) return
        _submitOrderCallback.postValue(Event(SubmitOrderState.Loading))
        submitOrderUseCase.execute(
            Consumer {
                if (it.success) {
                    _submitOrderCallback.value = Event(SubmitOrderState.Success(it.order))
                } else {
                    _submitOrderCallback.value =
                        Event(SubmitOrderState.ShowError(it.message.display()))
                }
            },
            Consumer {
                it.printStackTrace()
                _submitOrderCallback.value = Event(SubmitOrderState.ShowError(it.localizedMessage))
            },
            SubmitOrderUseCase.Param(order, wallet)
        )

    }
}