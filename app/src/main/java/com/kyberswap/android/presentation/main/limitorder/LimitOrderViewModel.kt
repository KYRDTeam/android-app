package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.CancelOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.CheckEligibleAddressUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFeeUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLocalLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetNonceUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetPendingBalancesUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetRelatedLimitOrdersUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.SubmitOrderUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetPlatformFeeUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import com.kyberswap.android.domain.usecase.wallet.CheckEligibleWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.common.MIN_SUPPORT_AMOUNT
import com.kyberswap.android.presentation.common.specialGasLimitDefault
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.balance.CheckEligibleWalletState
import com.kyberswap.android.presentation.main.profile.UserInfoState
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.presentation.main.swap.GetPlatformFeeState
import com.kyberswap.android.presentation.main.swap.SwapTokenTransactionState
import com.kyberswap.android.util.ErrorHandler
import com.kyberswap.android.util.ext.display
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.rounding
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigDecimal
import java.math.BigInteger
import java.net.UnknownHostException
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
    private val elegibleAddressUseCase: CheckEligibleAddressUseCase,
    private val checkEligibleWalletUseCase: CheckEligibleWalletUseCase,
    private val getPlatformFeeUseCase: GetPlatformFeeUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

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

    private val _getEligibleAddressCallback = MutableLiveData<Event<CheckEligibleAddressState>>()
    val getEligibleAddressCallback: LiveData<Event<CheckEligibleAddressState>>
        get() = _getEligibleAddressCallback

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

    private val _checkEligibleWalletCallback = MutableLiveData<Event<CheckEligibleWalletState>>()
    val checkEligibleWalletCallback: LiveData<Event<CheckEligibleWalletState>>
        get() = _checkEligibleWalletCallback

    private val _getPlatformFeeCallback = MutableLiveData<Event<GetPlatformFeeState>>()
    val getPlatformFeeCallback: LiveData<Event<GetPlatformFeeState>>
        get() = _getPlatformFeeCallback


    fun getLoginStatus() {
        getLoginStatusUseCase.dispose()
        getLoginStatusUseCase.execute(
            Consumer {
                _getLoginStatusCallback.value = Event(UserInfoState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getLoginStatusCallback.value =
                    Event(UserInfoState.ShowError(errorHandler.getError(it)))
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
                            errorHandler.getError(it)
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
                    getNonce(order, wallet)
                    currentLimitOrder = it
                },
                Consumer {
                    it.printStackTrace()
                    _getLocalLimitOrderCallback.value =
                        Event(GetLocalLimitOrderState.ShowError(errorHandler.getError(it)))
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
                Token.EXCHANGE_ETH_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
            else limitOrder.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
        val gasLimitEthToSource =
            if (limitOrder.tokenDest.gasLimit.toBigIntegerOrDefaultZero() == BigInteger.ZERO)
                Token.EXCHANGE_ETH_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
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
                    Event(CancelOrdersState.ShowError(errorHandler.getError(it)))
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
                    Event(
                        GetNonceState.ShowError(
                            errorHandler.getError(it),
                            it is UnknownHostException
                        )
                    )
            },
            GetNonceUseCase.Param(order, wallet)
        )
    }

    fun checkEligibleAddress(wallet: Wallet, isWalletChangeEvent: Boolean = true) {
        elegibleAddressUseCase.dispose()
        elegibleAddressUseCase.execute(
            Consumer {
                _getEligibleAddressCallback.value =
                    Event(CheckEligibleAddressState.Success(it, isWalletChangeEvent))

            },
            Consumer {
                it.printStackTrace()
                _getEligibleAddressCallback.value =
                    Event(
                        CheckEligibleAddressState.ShowError(
                            errorHandler.getError(it),
                            it is UnknownHostException
                        )
                    )
            },
            CheckEligibleAddressUseCase.Param(wallet)
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
                    Event(GetGasPriceState.ShowError(errorHandler.getError(it)))
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
                    Event(
                        GetRelatedOrdersState.ShowError(
                            errorHandler.getError(it),
                            it is UnknownHostException
                        )
                    )

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
                    Event(
                        GetMarketRateState.ShowError(
                            errorHandler.getError(it)
                        )
                    )
            },
            GetMarketRateUseCase.Param(
                order.tokenSource.symbol,
                order.tokenDest.symbol
            )
        )
    }

    fun getExpectedRate(
        order: LocalLimitOrder,
        srcAmount: String,
        platformFee: Int
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
                    Event(
                        GetExpectedRateState.ShowError(
                            errorHandler.getError(it),
                            it is UnknownHostException
                        )
                    )
            },
            GetExpectedRateUseCase.Param(
                order.userAddr,
                order.tokenSource,
                order.tokenDest,
                srcAmount,
                platformFee

            )
        )
    }

    fun getPlatformFee(order: LocalLimitOrder?) {
        if (order == null) return
        getPlatformFeeUseCase.dispose()
        getPlatformFeeUseCase.execute(
            Consumer {
                _getPlatformFeeCallback.value = Event(GetPlatformFeeState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getPlatformFeeCallback.value =
                    Event(GetPlatformFeeState.ShowError(it.localizedMessage))
            },
            GetPlatformFeeUseCase.Param(
                order.tokenSource.tokenAddress,
                order.tokenDest.tokenAddress
            )
        )
    }

    fun checkEligibleWallet(wallet: Wallet?) {
        if (wallet == null) return
        checkEligibleWalletUseCase.dispose()
        _checkEligibleWalletCallback.postValue(Event(CheckEligibleWalletState.Loading))
        checkEligibleWalletUseCase.execute(
            Consumer {
                _checkEligibleWalletCallback.value =
                    Event(CheckEligibleWalletState.Success(it))
            },
            Consumer {
                _checkEligibleWalletCallback.value =
                    Event(CheckEligibleWalletState.ShowError(errorHandler.getError(it)))
            },
            CheckEligibleWalletUseCase.Param(wallet)
        )
    }

    fun saveLimitOrder(
        order: LocalLimitOrder,
        fromContinue: Boolean = false,
        fromSwitchToken: Boolean = false
    ) {

        saveLimitOrderUseCase.execute(
            Action {
                if (fromContinue) {
                    _saveOrderCallback.value = Event(SaveLimitOrderState.Success(""))
                }
                if (fromSwitchToken) {
                    _swapTokenTransactionCallback.value =
                        Event(SwapTokenTransactionState.Success())
                }
            },
            Consumer {
                it.printStackTrace()
                _saveOrderCallback.value =
                    Event(SaveLimitOrderState.ShowError(errorHandler.getError(it)))
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
                _getFeeCallback.value = Event(
                    GetFeeState.ShowError(
                        errorHandler.getError(it),
                        isNetworkUnAvailable = it is UnknownHostException
                    )
                )

            },
            GetLimitOrderFeeUseCase.Param(
                order.tokenSource,
                order.tokenDest,
                (if (sourceAmount == order.tokenSource.currentBalance.toDisplayNumber()) order.tokenSource.sourceAmountWithoutRounding else sourceAmount)
                    ?: 0.toString(),
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
                _submitOrderCallback.value =
                    Event(SubmitOrderState.ShowError(it.localizedMessage))
            },
            SubmitOrderUseCase.Param(order, wallet)
        )
    }

    fun getGasLimit(wallet: Wallet?, order: LocalLimitOrder?, platformFee: Int) {
        if (wallet == null || order == null) return
        estimateGasUseCase.dispose()
        estimateGasUseCase.execute(
            Consumer {
                val gasLimit = it.toBigInteger()
                val specialGasLimit = specialGasLimitDefault(order.tokenSource, order.tokenDest)

                _getGetGasLimitCallback.value = Event(
                    GetGasLimitState.Success(
                        if (specialGasLimit != null) {
                            specialGasLimit.max(gasLimit)
                        } else {
                            gasLimit
                        }
                    )
                )

            },
            Consumer {
                it.printStackTrace()
                Event(GetGasLimitState.ShowError(errorHandler.getError(it)))
            },
            EstimateGasUseCase.Param(
                wallet,
                order.tokenSource,
                order.tokenDest,
                order.srcAmount,
                order.minRateWithPrecision,
                platformFee,
                DEFAULT_RESERVE_ROUTING_LIMIT_ORDER
            )
        )
    }

    fun convert(
        wallet: Wallet?,
        limitOrder: LocalLimitOrder,
        minConvertedAmount: BigDecimal,
        platformFee: Int
    ) {
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
                    Event(ConvertState.ShowError(errorHandler.getError(it)))
            },
            SwapTokenUseCase.Param(wallet!!, swap, platformFee, DEFAULT_RESERVE_ROUTING_LIMIT_ORDER)

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

        return if (availableAmount > MIN_SUPPORT_AMOUNT) {
            availableAmount.rounding().toDisplayNumber().exactAmount()
        } else BigDecimal.ZERO.toDisplayNumber()
    }

    public override fun onCleared() {
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
        checkEligibleWalletUseCase.dispose()
        pendingBalancesUseCase.dispose()
        elegibleAddressUseCase.dispose()
        super.onCleared()
    }

    companion object {
        const val DEFAULT_RESERVE_ROUTING_LIMIT_ORDER = true
    }
}