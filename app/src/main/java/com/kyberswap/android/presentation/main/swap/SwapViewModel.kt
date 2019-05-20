package com.kyberswap.android.presentation.main.swap

import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Cap
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.swap.*
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapUseCase
import com.kyberswap.android.presentation.common.DEFAULT_EXPECTED_RATE
import com.kyberswap.android.presentation.common.DEFAULT_MARKET_RATE
import com.kyberswap.android.presentation.common.DEFAULT_ROUNDING_NUMBER
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class SwapViewModel @Inject constructor(
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
    private var cap: Cap? = null
    private var gasLimit = BigInteger.ZERO

    private var _gas: Gas? = null
    val gas: Gas
        get() = _gas ?: Gas()

    val expectedRateDisplay: String
        get() = expectedRate.toBigDecimalOrDefaultZero()
            .setScale(2, BigDecimal.ROUND_UP).toPlainString()

    private val _saveSwapCallback = MutableLiveData<Event<SaveSwapState>>()
    val saveSwapDataCallback: LiveData<Event<SaveSwapState>>
        get() = _saveSwapCallback

    fun getMarketRate(srcToken: String, destToken: String) {
        getMarketRate.dispose()
        if (srcToken.isNotBlank() && destToken.isNotBlank()) {
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
                GetMarketRateUseCase.Param(srcToken, destToken)
            )

    }

    fun getCap(address: String?) {
        getCapUseCase.execute(
            Consumer {
                cap = it
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
                gasLimit = it.tokenSource.gasLimit.toBigDecimalOrDefaultZero().toBigInteger()
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
                _gas = it
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
        walletAddress: String,
        swap: Swap,
        srcAmount: String
    ) {
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
            GetExpectedRateUseCase.Param(walletAddress, swap, srcAmount)
        )
    }


    fun resetRate() {
        marketRate = null
        expectedRate = null
    }

    fun ratePercentage(): String {
        return expectedRate.percentage(marketRate).toPlainString()
    }

    fun verifyCap(amount: BigDecimal): Boolean {
        return cap != null && Convert.toWei(amount, Convert.Unit.ETHER) <= Convert.toWei(
            cap!!.cap,
            Convert.Unit.GWEI
        ) && !cap!!.rich
    }

    fun getExpectedDestAmount(amount: Editable?): BigDecimal {
        return if (expectedRate != null && !amount.isNullOrEmpty()) {
            amount.toString().toBigDecimalOrDefaultZero()
                .multiply(expectedRate.toBigDecimalOrDefaultZero())
                .setScale(DEFAULT_ROUNDING_NUMBER, BigDecimal.ROUND_UP)

 else BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_UP)
    }

    fun getExpectedDestUsdAmount(amount: Editable?, rateUsdNow: BigDecimal): BigDecimal {
        return getExpectedDestAmount(amount)
            .multiply(rateUsdNow)
            .setScale(2, BigDecimal.ROUND_UP)
    }


    fun rateThreshold(customRate: String): String {
        return (1.toDouble() - customRate.toBigDecimalOrDefaultZero().toDouble() / 100.toDouble()).toBigDecimal()
            .multiply(expectedRate.toBigDecimalOrDefaultZero())
            .setScale(2, BigDecimal.ROUND_UP)
            .toPlainString()
    }

    fun getGasLimit(wallet: Wallet, swap: Swap) {
        estimateGasUseCase.execute(
            Consumer {
                if (it.error == null) {
                    gasLimit = (it.amountUsed.toBigDecimal() * 1.2.toBigDecimal()).toBigInteger()
        

    ,
            Consumer {
                it.printStackTrace()
    ,
            EstimateGasUseCase.Param(wallet, swap)
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


    fun updateSwap(swap: Swap) {
        swap.marketRate = marketRate ?: DEFAULT_MARKET_RATE.toString()
        swap.expectedRate = expectedRate ?: DEFAULT_EXPECTED_RATE.toString()
        swap.gasLimit = gasLimit.toString()
        saveSwap(swap, true)

    }

}