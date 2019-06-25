package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.swap.*
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class SwapViewModel @Inject constructor(
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val getExpectedRateUseCase: GetExpectedRateUseCase,
    private val getSwapData: GetSwapDataUseCase,
    private val getMarketRate: GetMarketRateUseCase,
    private val saveSwapUseCase: SaveSwapUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val getCapUseCase: GetCapUseCase,
    private val estimateGasUseCase: EstimateGasUseCase,
    private val getWalletUseCase: GetSelectedWalletUseCase
) : ViewModel() {

    private val _getSwapCallback = MutableLiveData<Event<GetSwapState>>()
    val getSwapDataCallback: LiveData<Event<GetSwapState>>
        get() = _getSwapCallback


    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback


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


    private val _saveSwapCallback = MutableLiveData<Event<SaveSwapState>>()
    val saveSwapDataCallback: LiveData<Event<SaveSwapState>>
        get() = _saveSwapCallback

    private val _getWalletStateCallback = MutableLiveData<Event<GetWalletState>>()
    val getWalletStateCallback: LiveData<Event<GetWalletState>>
        get() = _getWalletStateCallback

    fun getMarketRate(swap: Swap) {

        if (swap.hasSamePair) {
            _getGetMarketRateCallback.value =
                Event(GetMarketRateState.Success(BigDecimal.ONE.toDisplayNumber()))
            return


        getMarketRate.dispose()
        if (swap.hasTokenPair) {
            getMarketRate.execute(
                Consumer {
                    _getGetMarketRateCallback.value = Event(GetMarketRateState.Success(it))
        ,
                Consumer {
                    it.printStackTrace()
                    _getGetMarketRateCallback.value =
                        Event(GetMarketRateState.ShowError(it.localizedMessage))
        ,
                GetMarketRateUseCase.Param(swap.tokenSource.tokenSymbol, swap.tokenDest.tokenSymbol)
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
        getSwapData.dispose()
        getSwapData.execute(
            Consumer {
                it.gasLimit = calculateGasLimit(it).toString()
                _getSwapCallback.value = Event(GetSwapState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getSwapCallback.value = Event(GetSwapState.ShowError(it.localizedMessage))
    ,
            GetSwapDataUseCase.Param(address)
        )
    }

    private fun calculateGasLimit(swap: Swap): BigInteger {
        val gasLimitSourceToEth =
            if (swap.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
                == BigInteger.ZERO
            )
                DEFAULT_GAS_LIMIT
            else swap.tokenSource.gasLimit.toBigIntegerOrDefaultZero()
        val gasLimitEthToSource =
            if (swap.tokenDest.gasLimit.toBigIntegerOrDefaultZero() == BigInteger.ZERO)
                DEFAULT_GAS_LIMIT
            else swap.tokenDest.gasLimit.toBigIntegerOrDefaultZero()

        return gasLimitSourceToEth + gasLimitEthToSource
    }

    fun getGasPrice() {
        getGasPriceUseCase.execute(
            Consumer {
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
            _getExpectedRateCallback.value =
                Event(GetExpectedRateState.Success(listOf(BigDecimal.ONE.toDisplayNumber())))
            return


        getExpectedRateUseCase.dispose()
        getExpectedRateUseCase.execute(
            Consumer {
                if (it.isNotEmpty() && it.first().toBigDecimalOrDefaultZero() > BigDecimal.ZERO) {
                    _getExpectedRateCallback.value = Event(GetExpectedRateState.Success(it))
        

    ,
            Consumer {
                it.printStackTrace()
                _getExpectedRateCallback.value =
                    Event(GetExpectedRateState.ShowError(it.localizedMessage))
    ,
            GetExpectedRateUseCase.Param(
                swap.walletAddress,
                swap.tokenSource,
                swap.tokenDest, srcAmount
            )
        )
    }

    fun getGasLimit(wallet: Wallet?, swap: Swap?) {
        if (wallet == null || swap == null) return
        estimateGasUseCase.execute(
            Consumer {
                if (it.error == null) {
                    val gasLimit =
                        if (swap.tokenSource.isDAI ||
                            swap.tokenSource.isTUSD ||
                            swap.tokenDest.isDAI ||
                            swap.tokenDest.isTUSD
                        ) {
                            swap.gasLimit.toBigIntegerOrDefaultZero().max(
                                (it.amountUsed.toBigDecimal().multiply(1.2.toBigDecimal()))
                                    .toBigInteger()
                            )
                 else {
                            (it.amountUsed.toBigDecimal().multiply(1.2.toBigDecimal())).toBigInteger()
                                .plus(100000.toBigInteger())
                
                    _getGetGasLimitCallback.value = Event(GetGasLimitState.Success(gasLimit))
        

    ,
            Consumer {
                it.printStackTrace()
                Event(GetGasLimitState.ShowError(it.localizedMessage))
    ,
            EstimateGasUseCase.Param(
                wallet,
                swap.tokenSource,
                swap.tokenDest,
                swap.sourceAmount,
                swap.minConversionRate
            )
        )
    }

    override fun onCleared() {
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

    fun getSelectedWallet() {
        getWalletUseCase.execute(
            Consumer { wallet ->
                _getWalletStateCallback.value = Event(GetWalletState.Success(wallet))

    ,
            Consumer {
                it.printStackTrace()
                _getWalletStateCallback.value =
                    Event(GetWalletState.ShowError(it.localizedMessage))
    ,
            null
        )
    }
}