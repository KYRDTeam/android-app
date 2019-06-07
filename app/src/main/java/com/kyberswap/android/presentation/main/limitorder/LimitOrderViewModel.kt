package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLocalLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderUseCase
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.DEFAULT_EXPECTED_RATE
import com.kyberswap.android.presentation.common.DEFAULT_MARKET_RATE
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.GetCapState
import com.kyberswap.android.presentation.main.swap.GetExpectedRateState
import com.kyberswap.android.presentation.main.swap.GetMarketRateState
import com.kyberswap.android.presentation.main.swap.SaveSwapState
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class LimitOrderViewModel @Inject constructor(
    private val getLimitOrderUseCase: GetLimitOrderDataUseCase,
    private val getLocalLimitOrderDataUseCase: GetLocalLimitOrderDataUseCase,
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val getExpectedRateUseCase: GetExpectedRateUseCase,
    private val getMarketRate: GetMarketRateUseCase,
    private val saveLimitOrderUseCase: SaveLimitOrderUseCase
) : ViewModel() {

    private val _getLocalLimitOrderCallback = MutableLiveData<Event<GetLocalLimitOrderState>>()
    val getLocalLimitOrderCallback: LiveData<Event<GetLocalLimitOrderState>>
        get() = _getLocalLimitOrderCallback


    private val _getExpectedRateCallback = MutableLiveData<Event<GetExpectedRateState>>()
    val getExpectedRateCallback: LiveData<Event<GetExpectedRateState>>
        get() = _getExpectedRateCallback


    private val _getCapCallback = MutableLiveData<Event<GetCapState>>()
    val getCapCallback: LiveData<Event<GetCapState>>
        get() = _getCapCallback


    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val _getGetMarketRateCallback = MutableLiveData<Event<GetMarketRateState>>()
    val getGetMarketRateCallback: LiveData<Event<GetMarketRateState>>
        get() = _getGetMarketRateCallback


    private var marketRate: String? = null
    private var expectedRate: String? = null

    private val _rate: String?
        get() = if (expectedRate.isNullOrEmpty()) marketRate else expectedRate

    val combineRate: String?
        get() = _rate.toBigDecimalOrDefaultZero().toDisplayNumber()

    private val _saveOrderCallback = MutableLiveData<Event<SaveSwapState>>()
    val saveSwapDataCallback: LiveData<Event<SaveSwapState>>
        get() = _saveOrderCallback

    val ratePercentage: String
        get() = expectedRate.percentage(marketRate).toDisplayNumber()

    fun getLimitOrders(walletAddress: String) {
        getLocalLimitOrderDataUseCase.execute(
            Consumer {
                _getLocalLimitOrderCallback.value = Event(GetLocalLimitOrderState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getLocalLimitOrderCallback.value =
                    Event(GetLocalLimitOrderState.ShowError(it.localizedMessage))
            },
            GetLocalLimitOrderDataUseCase.Param(walletAddress)
        )
    }

    fun getMarketRate(order: LocalLimitOrder) {

        if (order.hasSamePair) {
            marketRate = BigDecimal.ONE.toDisplayNumber()
            expectedRate = BigDecimal.ONE.toDisplayNumber()
            return
        }
        getMarketRate.dispose()
        if (order.hasSamePair) {
            getMarketRate.execute(
                Consumer {
                    marketRate = it
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
            marketRate = BigDecimal.ONE.toDisplayNumber()
            expectedRate = BigDecimal.ONE.toDisplayNumber()
            return
        }

        getExpectedRateUseCase.dispose()
        getExpectedRateUseCase.execute(
            Consumer {
                if (it.isNotEmpty()) {
                    expectedRate = it[0]
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
        getBalancePollingUseCase.dispose()
        getWalletByAddressUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun saveLimitOrder(order: LocalLimitOrder, fromContinue: Boolean = false) {
        saveLimitOrderUseCase.execute(
            Action {
                if (fromContinue) {
                    _saveOrderCallback.value = Event(SaveSwapState.Success(""))
                }
            },
            Consumer {
                it.printStackTrace()
                _saveOrderCallback.value = Event(SaveSwapState.ShowError(it.localizedMessage))
            },
            SaveLimitOrderUseCase.Param(order)
        )
    }

    fun setDefaultRate(order: LocalLimitOrder) {
        marketRate = order.marketRate
        expectedRate = order.expectedRate
    }


    fun updateOrder(order: LocalLimitOrder) {
        saveLimitOrder(updateLimitOrderRate(order), true)

    }

    private fun updateLimitOrderRate(order: LocalLimitOrder): LocalLimitOrder {
        return order.copy(
            marketRate = marketRate ?: DEFAULT_MARKET_RATE.toString(),
            expectedRate = expectedRate ?: DEFAULT_EXPECTED_RATE.toString()
        )
    }


    fun getExpectedDestAmount(amount: BigDecimal): BigDecimal {
        return amount.multiply(_rate.toBigDecimalOrDefaultZero())
    }


    fun getExpectedDestUsdAmount(amount: BigDecimal, rateUsdNow: BigDecimal): BigDecimal {
        return getExpectedDestAmount(amount)
            .multiply(rateUsdNow)
            .setScale(2, RoundingMode.UP)
    }

}