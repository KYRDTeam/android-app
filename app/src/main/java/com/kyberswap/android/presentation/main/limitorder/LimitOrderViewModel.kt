package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.swap.*
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.DEFAULT_EXPECTED_RATE
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.presentation.common.DEFAULT_MARKET_RATE
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.swap.*
import com.kyberswap.android.util.ext.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

class LimitOrderViewModel @Inject constructor(
    private val getLimitOrderUseCase: GetLimitOrderDataUseCase,
    private val getBalancePollingUseCase: GetBalancePollingUseCase,
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val getExpectedRateUseCase: GetExpectedRateUseCase,
    private val getSwapData: GetSwapDataUseCase,
    private val getMarketRate: GetMarketRateUseCase,
    private val saveSwapUseCase: SaveSwapUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val getCapUseCase: GetCapUseCase,
    private val estimateGasUseCase: EstimateGasUseCase
) : ViewModel() {

    private val _getSwapCallback = MutableLiveData<Event<GetSwapState>>()
    val getSwapDataCallback: LiveData<Event<GetSwapState>>
        get() = _getSwapCallback


    private val _getExpectedRateCallback = MutableLiveData<Event<GetExpectedRateState>>()
    val getExpectedRateCallback: LiveData<Event<GetExpectedRateState>>
        get() = _getExpectedRateCallback


    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback


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
    private var gasLimit = BigInteger.ZERO

    private val _rate: String?
        get() = if (expectedRate.isNullOrEmpty()) marketRate else expectedRate

    val combineRate: String?
        get() = _rate.toBigDecimalOrDefaultZero().toDisplayNumber()

    private val _saveSwapCallback = MutableLiveData<Event<SaveSwapState>>()
    val saveSwapDataCallback: LiveData<Event<SaveSwapState>>
        get() = _saveSwapCallback

    val ratePercentage: String
        get() = expectedRate.percentage(marketRate).toDisplayNumber()

    fun getLimitOrders(walletAddress: String) {
        getLimitOrderUseCase.execute(
            Consumer {
                Timber.e(it.toString())
    ,
            Consumer {
                it.printStackTrace()
                Timber.e(it.localizedMessage)
    ,
            GetLimitOrderDataUseCase.Param(walletAddress)
        )
    }

    fun getMarketRate(swap: Swap) {

        if (swap.hasSamePair) {
            marketRate = BigDecimal.ONE.toDisplayNumber()
            expectedRate = BigDecimal.ONE.toDisplayNumber()
            return

        getMarketRate.dispose()
        if (swap.hasTokenPair) {
            getMarketRate.execute(
                Consumer {
                    marketRate = it
                    _getGetMarketRateCallback.value = Event(GetMarketRateState.Success(it))
        ,
                Consumer {
                    it.printStackTrace()
                    _getGetMarketRateCallback.value =
                        Event(GetMarketRateState.ShowError(it.localizedMessage))
        ,
                GetMarketRateUseCase.Param(swap)
            )

    }

    fun getCap(address: String?) {
        getCapUseCase.execute(
            Consumer {
                _getCapCallback.value = Event(GetCapState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getCapCallback.value = Event(GetCapState.ShowError(it.localizedMessage))
    ,
            GetCapUseCase.Param(address)
        )
    }

    fun getSwapData(address: String) {
        getSwapData.execute(
            Consumer {
                gasLimit = if (it.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
                    == BigInteger.ZERO
                ) DEFAULT_GAS_LIMIT
                else it.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
                _getSwapCallback.value = Event(GetSwapState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getSwapCallback.value = Event(GetSwapState.ShowError(it.localizedMessage))
    ,
            GetSwapDataUseCase.Param(address)
        )
    }

    fun getGasPrice() {
        getGasPriceUseCase.execute(
            Consumer {
                //                _gas = it
                _getGetGasPriceCallback.value = Event(GetGasPriceState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getGetGasPriceCallback.value =
                    Event(GetGasPriceState.ShowError(it.localizedMessage))
    ,
            null
        )
    }

    fun getExpectedRate(
        swap: Swap,
        srcAmount: String
    ) {
        if (swap.hasSamePair) {
            marketRate = BigDecimal.ONE.toDisplayNumber()
            expectedRate = BigDecimal.ONE.toDisplayNumber()
            return


        getExpectedRateUseCase.dispose()
        getExpectedRateUseCase.execute(
            Consumer {
                if (it.isNotEmpty()) {
                    expectedRate = it[0]
        
                _getExpectedRateCallback.value = Event(GetExpectedRateState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getExpectedRateCallback.value =
                    Event(GetExpectedRateState.ShowError(it.localizedMessage))
    ,
            GetExpectedRateUseCase.Param(swap.walletAddress, swap, srcAmount)
        )
    }

    fun getGasLimit(wallet: Wallet, swap: Swap) {
        estimateGasUseCase.execute(
            Consumer {
                if (it.error == null) {
                    gasLimit =
                        if (swap.tokenSource.isDAI() ||
                            swap.tokenSource.isTUSD() ||
                            swap.tokenDest.isDAI() ||
                            swap.tokenDest.isTUSD()
                        ) {
                            gasLimit.max(
                                (it.amountUsed.toBigDecimal() * 1.2.toBigDecimal())
                                    .toBigInteger()
                            )
                 else {
                            it.amountUsed
                
        

    ,
            Consumer {
                it.printStackTrace()
    ,
            EstimateGasUseCase.Param(wallet, updateSwapRate(swap))
        )
    }

    override fun onCleared() {
        getBalancePollingUseCase.dispose()
        getWalletByAddressUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun saveSwap(swap: Swap, fromContinue: Boolean = false) {
        saveSwapUseCase.execute(
            Action {
                if (fromContinue) {
                    _saveSwapCallback.value = Event(SaveSwapState.Success(""))
        
    ,
            Consumer {
                it.printStackTrace()
                _saveSwapCallback.value = Event(SaveSwapState.ShowError(it.localizedMessage))
    ,
            SaveSwapUseCase.Param(swap)
        )
    }

    fun setDefaultRate(swap: Swap) {
        marketRate = swap.marketRate
        expectedRate = swap.expectedRate
        gasLimit = swap.gasLimit.toBigIntegerOrDefaultZero()
    }


    fun updateSwap(swap: Swap) {
        saveSwap(updateSwapRate(swap), true)

    }

    private fun updateSwapRate(swap: Swap): Swap {
        return swap.copy(
            marketRate = marketRate ?: DEFAULT_MARKET_RATE.toString(),
            expectedRate = expectedRate ?: DEFAULT_EXPECTED_RATE.toString(),
            gasLimit = gasLimit.toString()
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

    fun rateThreshold(customRate: String): String {
        return (1.toDouble() - customRate.toDoubleOrDefaultZero() / 100.toDouble())
            .toBigDecimal()
            .multiply(_rate.toBigDecimalOrDefaultZero())
            .toDisplayNumber()
    }

}