package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.GetMarketsUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetStableQuoteTokensUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveMarketItemUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveSelectedMarketUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class MarketViewModel @Inject constructor(
    private val getMarkets: GetMarketsUseCase,
    private val getStableQuoteTokensUseCase: GetStableQuoteTokensUseCase,
    private val saveMarketItemUseCase: SaveMarketItemUseCase,
    private val saveSelectedMarketUseCase: SaveSelectedMarketUseCase,
    selectedWalletViewModel: GetSelectedWalletUseCase,
    private val errorHandler: ErrorHandler

) : SelectedWalletViewModel(selectedWalletViewModel, errorHandler) {

    private val _getMarketsCallback = MutableLiveData<Event<GetMarketsState>>()
    val getMarketsCallback: LiveData<Event<GetMarketsState>>
        get() = _getMarketsCallback

    private val _getQuotesCallback = MutableLiveData<Event<GetQuoteTokensState>>()
    val getQuotesCallback: LiveData<Event<GetQuoteTokensState>>
        get() = _getQuotesCallback

    private val _saveSelectedMarketCallback = MutableLiveData<Event<SaveSelectedMarketState>>()
    val saveSelectedMarketCallback: LiveData<Event<SaveSelectedMarketState>>
        get() = _saveSelectedMarketCallback

    private val _saveFavMarketCallback = MutableLiveData<Event<SaveFavMarketState>>()
    val saveFavMarketCallback: LiveData<Event<SaveFavMarketState>>
        get() = _saveFavMarketCallback

    val currentMarketLiveData = MutableLiveData<Event<String>>()

    fun getMarkets(isForceRefresh: Boolean = false) {
        getMarkets.dispose()
        getMarkets.execute(
            Consumer {
                _getMarketsCallback.value = Event(GetMarketsState.Success(it))
            },
            Consumer {
                _getMarketsCallback.value =
                    Event(GetMarketsState.ShowError(errorHandler.getError(it)))
            },
            isForceRefresh
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

    fun saveFav(marketItem: MarketItem) {
        saveMarketItemUseCase.dispose()
        saveMarketItemUseCase.execute(
            Action {
                _saveFavMarketCallback.value = Event(SaveFavMarketState.Success(marketItem.isFav))
            },
            Consumer {
                _saveFavMarketCallback.value =
                    Event(SaveFavMarketState.ShowError(errorHandler.getError(it)))
            },
            SaveMarketItemUseCase.Param(marketItem)
        )
    }

    fun saveSelectedMarket(wallet: Wallet, marketItem: MarketItem) {
        saveSelectedMarketUseCase.dispose()
        saveSelectedMarketUseCase.execute(
            Action {
                _saveSelectedMarketCallback.value = Event(SaveSelectedMarketState.Success())
            },
            Consumer {
                it.printStackTrace()
                _saveSelectedMarketCallback.value =
                    Event(SaveSelectedMarketState.ShowError(it.localizedMessage))
            },
            SaveSelectedMarketUseCase.Param(wallet, marketItem)
        )
    }

    override fun onCleared() {
        getMarkets.dispose()
        getStableQuoteTokensUseCase.dispose()
        saveMarketItemUseCase.dispose()
        saveSelectedMarketUseCase.dispose()
        super.onCleared()
    }
}