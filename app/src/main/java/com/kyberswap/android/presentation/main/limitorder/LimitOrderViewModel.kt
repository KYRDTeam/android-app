package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.usecase.limitorder.*
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.swap.*
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.*
import com.kyberswap.android.util.ext.display
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigDecimal
import java.math.BigInteger
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
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val estimateGasUseCase: EstimateGasUseCase,
    private val swapTokenUseCase: SwapTokenUseCase,
    private val getLoginStatusUseCase: GetLoginStatusUseCase,
    private val pendingBalancesUseCase: GetPendingBalancesUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase) {

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

    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    private val _cancelRelatedOrderCallback = MutableLiveData<Event<CancelOrdersState>>()
    val cancelRelatedOrderCallback: LiveData<Event<CancelOrdersState>>
        get() = _cancelRelatedOrderCallback

    val compositeDisposable = CompositeDisposable()

    private val _getGetMarketRateCallback = MutableLiveData<Event<GetMarketRateState>>()
    val getGetMarketRateCallback: LiveData<Event<GetMarketRateState>>
        get() = _getGetMarketRateCallback


    private val _getFeeCallback = MutableLiveData<Event<GetFeeState>>()
    val getFeeCallback: LiveData<Event<GetFeeState>>
        get() = _getFeeCallback

    private val _saveOrderCallback = MutableLiveData<Event<SaveLimitOrderState>>()
    val saveOrderCallback: LiveData<Event<SaveLimitOrderState>>
        get() = _saveOrderCallback

    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback

    private val _swapTokenTransactionCallback =
        MutableLiveData<Event<SwapTokenTransactionState>>()
    val swapTokenTransactionCallback: LiveData<Event<SwapTokenTransactionState>>
        get() = _swapTokenTransactionCallback

    private val _convertCallback =
        MutableLiveData<Event<ConvertState>>()
    val convertCallback: LiveData<Event<ConvertState>>
        get() = _convertCallback

    private val _getLoginStatusCallback = MutableLiveData<Event<UserInfoState>>()
    val getLoginStatusCallback: LiveData<Event<UserInfoState>>
        get() = _getLoginStatusCallback

    private val _getPendingBalancesCallback = MutableLiveData<Event<GetPendingBalancesState>>()
    val getPendingBalancesCallback: LiveData<Event<GetPendingBalancesState>>
        get() = _getPendingBalancesCallback

    var relatedOrders = mutableListOf<Order>()

    private var currentLimitOrder: LocalLimitOrder? = null


    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun getPendingBalances(wallet: Wallet?) {
        if (wallet == null) return
        pendingBalancesUseCase.dispose()
        pendingBalancesUseCase.execute(
            Consumer {
                _getPendingBalancesCallback.value =
                    Event(GetPendingBalancesState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getPendingBalancesCallback.value =
                    Event(
                        GetPendingBalancesState.ShowError(
                            it.localizedMessage
                        )
                    )
            },
            GetPendingBalancesUseCase.Param(wallet)
        )
    }

    fun getLimitOrders(wallet: Wallet?) {
        getLocalLimitOrderDataUseCase.dispose()
        wallet?.let {
            _getLocalLimitOrderCallback.postValue(Event(GetLocalLimitOrderState.Loading))
            getLocalLimitOrderDataUseCase.execute(
                Consumer {
                    val order = it.copy(gasLimit = calculateGasLimit(it))
                    _getLocalLimitOrderCallback.value =
                        Event(GetLocalLimitOrderState.Success(order))
                    getRelatedOrders(order, wallet)
                    getNonce(order, wallet)
                    currentLimitOrder = it
                },
                Consumer {
                    it.printStackTrace()
                    _getLocalLimitOrderCallback.value =
                        Event(GetLocalLimitOrderState.ShowError(it.localizedMessage))
                },
                GetLocalLimitOrderDataUseCase.Param(wallet)
            )
        }

    }

    private fun calculateGasLimit(limitOrder: LocalLimitOrder): BigInteger {
        val gasLimitSourceToEth =
            if (limitOrder.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
                == BigInteger.ZERO
            )
                DEFAULT_GAS_LIMIT
            else limitOrder.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
        val gasLimitEthToSource =
            if (limitOrder.tokenDest.gasLimit.toBigIntegerOrDefaultZero() == BigInteger.ZERO)
                DEFAULT_GAS_LIMIT
            else limitOrder.tokenDest.gasLimit.toBigIntegerOrDefaultZero()

        return gasLimitSourceToEth + gasLimitEthToSource
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
        getNonceUseCase.dispose()
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

    fun getGasPrice() {
        getGasPriceUseCase.dispose()
        getGasPriceUseCase.execute(
            Consumer {
                _getGetGasPriceCallback.value = Event(GetGasPriceState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getGetGasPriceCallback.value =
                    Event(GetGasPriceState.ShowError(it.localizedMessage))
            },
            null
        )
    }


    fun getRelatedOrders(order: LocalLimitOrder, wallet: Wallet) {
        getRelatedLimitOrdersUseCase.dispose()
        _getRelatedOrderCallback.postValue(Event(GetRelatedOrdersState.Loading))
        getRelatedLimitOrdersUseCase.execute(
            Consumer { orderList ->
                relatedOrders.clear()
                relatedOrders.addAll(orderList)
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.Success(toOrderItems(orderList)))
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

    fun toOrderItems(orders: List<Order>): List<OrderItem> {
        return orders.sortedByDescending {
            it.time
        }.groupBy { it.shortedDateTimeFormat }
            .flatMap { item ->
                val items = mutableListOf<OrderItem>()
                items.add(OrderItem.Header(item.key))
                val list = item.value.sortedByDescending { it.time }
                list.forEachIndexed { index, transaction ->
                    if (index % 2 == 0) {
                        items.add(OrderItem.ItemEven(transaction))
                    } else {
                        items.add(OrderItem.ItemOdd(transaction))
                    }
                }
                items
            }
    }

    fun getMarketRate(order: LocalLimitOrder) {

        if (order.hasSamePair) {
            _getGetMarketRateCallback.value =
                Event(GetMarketRateState.Success(BigDecimal.ONE.toDisplayNumber()))
            return
        }
        getMarketRate.dispose()
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
                order.tokenSource.symbol,
                order.tokenDest.symbol
            )
        )
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
                if (it.isNotEmpty() && it[0].toBigDecimalOrDefaultZero() > BigDecimal.ZERO) {
                    _getExpectedRateCallback.value = Event(GetExpectedRateState.Success(it))
                }
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

    fun saveLimitOrder(
        order: LocalLimitOrder,
        fromContinue: Boolean = false,
        fromConvert: Boolean = false
    ) {

        saveLimitOrderUseCase.execute(
            Action {
                if (fromContinue) {
                    _saveOrderCallback.value = Event(SaveLimitOrderState.Success(""))
                }
                if (fromConvert) {
                    _swapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.Success(""))
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
        getLimitOrderFee.dispose()
        _getFeeCallback.postValue(Event(GetFeeState.Loading))
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

    fun warningOrderList(rate: BigDecimal, pendingOrders: List<Order>): List<Order> {
        return pendingOrders.filter {
            it.minRate > rate
        }
    }

    fun needConvertWETH(order: LocalLimitOrder?, pendingBalances: PendingBalances?): Boolean {
        if (order == null) return false
        val pendingAmount =
            pendingBalances?.data?.get(order.tokenSource.symbol) ?: BigDecimal.ZERO
        return order.tokenSource.isETHWETH &&
            (order.wethToken.currentBalance - pendingAmount - order.srcAmount.toBigDecimalOrDefaultZero() < BigDecimal.ZERO)
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

    fun getGasLimit(wallet: Wallet?, order: LocalLimitOrder?) {
        if (wallet == null || order == null) return
        estimateGasUseCase.dispose()
        estimateGasUseCase.execute(
            Consumer {
                if (it.error == null) {
                    val gasLimit =
                        if (order.tokenSource.isDAI ||
                            order.tokenSource.isTUSD ||
                            order.tokenDest.isDAI ||
                            order.tokenDest.isTUSD
                        ) {
                            order.gasLimit.max(
                                (it.amountUsed.toBigDecimal().multiply(1.2.toBigDecimal())).toBigInteger()
                            )
                        } else {
                            (it.amountUsed.toBigDecimal().multiply(1.2.toBigDecimal())).toBigInteger()
                        }

                    _getGetGasLimitCallback.value = Event(GetGasLimitState.Success(gasLimit))
                }

            },
            Consumer {
                it.printStackTrace()
                Event(GetGasLimitState.ShowError(it.localizedMessage))
            },
            EstimateGasUseCase.Param(
                wallet,
                order.tokenSource,
                order.tokenDest,
                order.srcAmount,
                order.minRateWithPrecision
            )
        )
    }

    fun convert(wallet: Wallet?, limitOrder: LocalLimitOrder, minConvertedAmount: BigDecimal) {
        val swap = Swap(limitOrder, minConvertedAmount)
        _convertCallback.postValue(Event(ConvertState.Loading))
        swapTokenUseCase.execute(
            Consumer {
                val wethBalance =
                    limitOrder.minConvertedAmount.toBigDecimalOrDefaultZero() + limitOrder.wethToken.currentBalance
                val order = limitOrder.copy(
                    wethToken = limitOrder.wethToken.updateBalance(
                        wethBalance
                    )
                )

                _convertCallback.value = Event(ConvertState.Success(order))

            },
            Consumer {
                it.printStackTrace()
                _convertCallback.value =
                    Event(ConvertState.ShowError(it.localizedMessage))
            },
            SwapTokenUseCase.Param(wallet!!, swap)

        )
    }

    fun calAvailableAmount(token: Token?, pendingBalances: PendingBalances?): String {
        val pendingAmount =
            pendingBalances?.data?.get(token?.symbol) ?: BigDecimal.ZERO
        val currentAmount = token?.currentBalance ?: BigDecimal.ZERO
        var availableAmount = currentAmount - pendingAmount
        if (availableAmount < BigDecimal.ZERO) {
            availableAmount = BigDecimal.ZERO
        }

        return availableAmount.toDisplayNumber()
    }

    fun cancelHigherRateOrder(rate: BigDecimal) {
        val warningOrderList = warningOrderList(
            rate,
            relatedOrders
        )

        var numOfSuccess = 0

        warningOrderList.forEach {
            cancelOrderUseCase.execute(
                Consumer {
                    numOfSuccess++
                    if (numOfSuccess == warningOrderList.size) {
                        _cancelRelatedOrderCallback.value =
                            Event(CancelOrdersState.Success(Cancelled(true)))
                    }
                },
                Consumer {
                    it.printStackTrace()
                    _cancelRelatedOrderCallback.value =
                        Event(CancelOrdersState.ShowError(it.localizedMessage))
                    cancelOrderUseCase.dispose()
                },
                CancelOrderUseCase.Param(it)
            )
        }
    }

    override fun onCleared() {
        getRelatedLimitOrdersUseCase.dispose()
        getLocalLimitOrderDataUseCase.dispose()
        getWalletByAddressUseCase.dispose()
        getExpectedRateUseCase.dispose()
        getMarketRate.dispose()
        saveLimitOrderUseCase.dispose()
        getLimitOrderFee.dispose()
        submitOrderUseCase.dispose()
        getNonceUseCase.dispose()
        cancelOrderUseCase.dispose()
        getGasPriceUseCase.dispose()
        estimateGasUseCase.dispose()
        swapTokenUseCase.dispose()
        getLoginStatusUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

}