package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFeeUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLocalLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetNonceUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetPendingBalancesUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetRelatedLimitOrdersUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetSelectedMarketUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetStableQuoteTokensUseCase
import com.kyberswap.android.domain.usecase.limitorder.PollingMarketsUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetGasPriceUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.common.MIN_SUPPORT_AMOUNT
import com.kyberswap.android.presentation.common.specialGasLimitDefault
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetGasLimitState
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.util.ErrorHandler
import com.kyberswap.android.util.ext.exactAmount
import com.kyberswap.android.util.ext.rounding
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.functions.Consumer
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.net.UnknownHostException
import javax.inject.Inject

class LimitOrderTypeViewModel @Inject constructor(
    private val pollingMarketsUseCase: PollingMarketsUseCase,
    private val getStableQuoteTokensUseCase: GetStableQuoteTokensUseCase,
    private val getLocalLimitOrderDataUseCase: GetLocalLimitOrderDataUseCase,
    private val getNonceUseCase: GetNonceUseCase,
    getSelectedWalletUseCase: GetSelectedWalletUseCase,
    private val pendingBalancesUseCase: GetPendingBalancesUseCase,
    private val getLimitOrderFee: GetLimitOrderFeeUseCase,
    private val getMarketRateUseCase: GetMarketRateUseCase,
    private val getExpectedRateUseCase: GetExpectedRateUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val estimateGasUseCase: EstimateGasUseCase,
    private val getRelatedLimitOrdersUseCase: GetRelatedLimitOrdersUseCase,
    private val getSelectedMarketUseCase: GetSelectedMarketUseCase,
    private val errorHandler: ErrorHandler

) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _getMarketsCallback = MutableLiveData<Event<GetMarketsState>>()
    val getMarketsCallback: LiveData<Event<GetMarketsState>>
        get() = _getMarketsCallback

    private val _getQuotesCallback = MutableLiveData<Event<GetQuoteTokensState>>()
    val getQuotesCallback: LiveData<Event<GetQuoteTokensState>>
        get() = _getQuotesCallback

    private val _getLocalLimitOrderCallback = MutableLiveData<Event<GetLocalLimitOrderState>>()
    val getLocalLimitOrderCallback: LiveData<Event<GetLocalLimitOrderState>>
        get() = _getLocalLimitOrderCallback

    private val _getGetNonceStateCallback = MutableLiveData<Event<GetNonceState>>()
    val getGetNonceStateCallback: LiveData<Event<GetNonceState>>
        get() = _getGetNonceStateCallback

    private val _getPendingBalancesCallback = MutableLiveData<Event<GetPendingBalancesState>>()
    val getPendingBalancesCallback: LiveData<Event<GetPendingBalancesState>>
        get() = _getPendingBalancesCallback

    private val _getFeeCallback = MutableLiveData<Event<GetFeeState>>()
    val getFeeCallback: LiveData<Event<GetFeeState>>
        get() = _getFeeCallback

    private val _getExpectedRateCallback = MutableLiveData<Event<GetExpectedRateState>>()
    val getExpectedRateCallback: LiveData<Event<GetExpectedRateState>>
        get() = _getExpectedRateCallback

    private val _getGetMarketRateCallback = MutableLiveData<Event<GetMarketRateState>>()
    val getGetMarketRateCallback: LiveData<Event<GetMarketRateState>>
        get() = _getGetMarketRateCallback

    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback

    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    private val _getRelatedOrderCallback = MutableLiveData<Event<GetRelatedOrdersState>>()
    val getRelatedOrderCallback: LiveData<Event<GetRelatedOrdersState>>
        get() = _getRelatedOrderCallback

    var relatedOrders = mutableListOf<Order>()

    private val _getSelectedMarketCallback = MutableLiveData<Event<GetSelectedMarketState>>()
    val getSelectedMarketCallback: LiveData<Event<GetSelectedMarketState>>
        get() = _getSelectedMarketCallback

    fun getMarkets() {
        pollingMarketsUseCase.dispose()
        pollingMarketsUseCase.execute(
            Consumer {
                _getMarketsCallback.value = Event(GetMarketsState.Success(it))
            },
            Consumer {
                _getMarketsCallback.value =
                    Event(GetMarketsState.ShowError(errorHandler.getError(it)))
            },
            null
        )
    }

    fun getStableQuoteTokens() {
        getStableQuoteTokensUseCase.dispose()
        getStableQuoteTokensUseCase.execute(
            Consumer {
                _getQuotesCallback.value = Event(GetQuoteTokensState.Success(it))
            },
            Consumer {
                _getQuotesCallback.value = Event(GetQuoteTokensState.ShowError(it.localizedMessage))
            },
            null
        )
    }

    fun getLimitOrder(wallet: Wallet?, type: Int) {
        if (wallet == null) return
        getLocalLimitOrderDataUseCase.dispose()
        getLocalLimitOrderDataUseCase.execute(
            Consumer {
                val order = it.copy(gasLimit = calculateGasLimit(it))
                _getLocalLimitOrderCallback.value =
                    Event(GetLocalLimitOrderState.Success(order))
                getNonce(order, wallet)
            },
            Consumer {
                Timber.e(it.localizedMessage)
                _getLocalLimitOrderCallback.value =
                    Event(GetLocalLimitOrderState.ShowError(errorHandler.getError(it)))
            },
            GetLocalLimitOrderDataUseCase.Param(wallet, type)
        )
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

    fun getSelectedMarket(wallet: Wallet?) {
        if (wallet == null) return
        getSelectedMarketUseCase.dispose()
        getSelectedMarketUseCase.execute(
            Consumer {
                _getSelectedMarketCallback.value = Event(GetSelectedMarketState.Success(it))
            },
            Consumer {
                Timber.e(it.localizedMessage)
                it.printStackTrace()
                _getSelectedMarketCallback.value =
                    Event(GetSelectedMarketState.ShowError(errorHandler.getError(it)))
            },
            GetSelectedMarketUseCase.Param(wallet)
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

    fun getGasLimit(wallet: Wallet?, order: LocalLimitOrder?) {
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
                order.minRateWithPrecision
            )
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

    fun getMarketRate(order: LocalLimitOrder) {

        if (order.hasSamePair) {
            _getGetMarketRateCallback.value =
                Event(GetMarketRateState.Success(BigDecimal.ONE.toDisplayNumber()))
            return
        }
        getMarketRateUseCase.dispose()
        getMarketRateUseCase.execute(
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
        srcAmount: String
    ) {
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
                srcAmount
            )
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

    override fun onCleared() {
        pollingMarketsUseCase.dispose()
        getStableQuoteTokensUseCase.dispose()
        getLocalLimitOrderDataUseCase.dispose()
        getNonceUseCase.dispose()
        pendingBalancesUseCase.dispose()
        getMarketRateUseCase.dispose()
        getExpectedRateUseCase.dispose()
        getGasPriceUseCase.dispose()
        getRelatedLimitOrdersUseCase.dispose()
        getSelectedMarketUseCase.dispose()
        super.onCleared()
    }
}