package com.kyberswap.android.presentation.main.swap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.alert.GetAlertUseCase
import com.kyberswap.android.domain.usecase.swap.*
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.domain.usecase.wallet.GetWalletByAddressUseCase
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.presentation.main.alert.GetAlertState
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toLongSafe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class SwapViewModel @Inject constructor(
    private val getWalletByAddressUseCase: GetWalletByAddressUseCase,
    private val getExpectedRateUseCase: GetExpectedRateUseCase,
    private val getSwapDataUseCase: GetSwapDataUseCase,
    private val getMarketRate: GetMarketRateUseCase,
    private val saveSwapUseCase: SaveSwapUseCase,
    private val getGasPriceUseCase: GetGasPriceUseCase,
    private val getCapUseCase: GetCapUseCase,
    private val estimateGasUseCase: EstimateGasUseCase,
    private val getAlertUseCase: GetAlertUseCase,
    getWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getWalletUseCase) {

    private val _getSwapCallback = MutableLiveData<Event<GetSwapState>>()
    val getSwapDataCallback: LiveData<Event<GetSwapState>>
        get() = _getSwapCallback


    private val _getGetGasLimitCallback = MutableLiveData<Event<GetGasLimitState>>()
    val getGetGasLimitCallback: LiveData<Event<GetGasLimitState>>
        get() = _getGetGasLimitCallback

    private val _getGetGasPriceCallback = MutableLiveData<Event<GetGasPriceState>>()
    val getGetGasPriceCallback: LiveData<Event<GetGasPriceState>>
        get() = _getGetGasPriceCallback


    private val _getCapCallback = MutableLiveData<Event<GetCapState>>()
    val getCapCallback: LiveData<Event<GetCapState>>
        get() = _getCapCallback

    private val _getAlertState = MutableLiveData<Event<GetAlertState>>()
    val getAlertState: LiveData<Event<GetAlertState>>
        get() = _getAlertState

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val _getGetMarketRateCallback = MutableLiveData<Event<GetMarketRateState>>()
    val getGetMarketRateCallback: LiveData<Event<GetMarketRateState>>
        get() = _getGetMarketRateCallback

    private val _getExpectedRateCallback = MutableLiveData<Event<GetExpectedRateState>>()
    val getExpectedRateCallback: LiveData<Event<GetExpectedRateState>>
        get() = _getExpectedRateCallback

    private val _saveSwapCallback = MutableLiveData<Event<SaveSwapState>>()
    val saveSwapDataCallback: LiveData<Event<SaveSwapState>>
        get() = _saveSwapCallback

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

    fun getSwapData(wallet: Wallet, alert: NotificationAlert? = null) {
        getSwapDataUseCase.dispose()
        getSwapDataUseCase.execute(
            Consumer {
                it.gasLimit = calculateGasLimit(it).toString()
                _getSwapCallback.value = Event(GetSwapState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getSwapCallback.value = Event(GetSwapState.ShowError(it.localizedMessage))
    ,
            GetSwapDataUseCase.Param(wallet, alert)
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

    fun getAlert(alertNotification: NotificationAlert) {
        getAlertUseCase.execute(
            Consumer {
                _getAlertState.value = Event(GetAlertState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getAlertState.value = Event(GetAlertState.ShowError(it.localizedMessage))
    ,
            GetAlertUseCase.Param(
                if (alertNotification.alertId > 0) alertNotification.alertId else alertNotification.testAlertId.toLongSafe()

            )
        )
    }

    override fun onCleared() {
        getWalletByAddressUseCase.dispose()
        getExpectedRateUseCase.dispose()
        getSwapDataUseCase.dispose()
        getMarketRate.dispose()
        saveSwapUseCase.dispose()
        getGasPriceUseCase.dispose()
        estimateGasUseCase.dispose()
        getCapUseCase.dispose()
        getAlertUseCase.dispose()
        compositeDisposable.dispose()
        super.onCleared()
    }
}