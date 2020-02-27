package com.kyberswap.android.presentation.main.balance.chart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.usecase.token.GetChartDataForTokenUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSelectedWalletUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.SelectedWalletViewModel
import com.kyberswap.android.util.ErrorHandler
import io.reactivex.functions.Consumer
import javax.inject.Inject

class LineChartViewModel @Inject constructor(
    private val getChartDataForTokenUseCase: GetChartDataForTokenUseCase,
    private val errorHandler: ErrorHandler,
    getSelectedWalletUseCase: GetSelectedWalletUseCase
) : SelectedWalletViewModel(getSelectedWalletUseCase, errorHandler) {

    private val _getChartCallback = MutableLiveData<Event<GetChartState>>()
    val getChartCallback: LiveData<Event<GetChartState>>
        get() = _getChartCallback

    fun getChartData(symbol: String?, chartType: ChartType?) {
        if (symbol == null || chartType == null) return
        getChartDataForTokenUseCase.execute(
            Consumer {
                _getChartCallback.value = Event(GetChartState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getChartCallback.value = Event(GetChartState.ShowError(errorHandler.getError(it)))
            },
            GetChartDataForTokenUseCase.Param(symbol, chartType)
        )
    }

    override fun onCleared() {
        getChartDataForTokenUseCase.dispose()
        super.onCleared()
    }
}