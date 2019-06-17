package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFilterUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderFilterUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class FilterViewModel @Inject constructor(
    private val getLimitOrderFilterUseCase: GetLimitOrderFilterUseCase,
    private val saveLimitOrderFilterUseCase: SaveLimitOrderFilterUseCase
) : ViewModel() {

    private val _getFilterStateCallback = MutableLiveData<Event<GetFilterState>>()
    val getFilterStateCallback: LiveData<Event<GetFilterState>>
        get() = _getFilterStateCallback

    private val _saveFilterStateCallback = MutableLiveData<Event<SaveFilterState>>()
    val saveFilterStateCallback: LiveData<Event<SaveFilterState>>
        get() = _saveFilterStateCallback

    fun getFilter(wallet: Wallet) {
        getLimitOrderFilterUseCase.execute(
            Consumer {
                _getFilterStateCallback.value = Event(GetFilterState.Success(it))
    ,
            Consumer {
                it.printStackTrace()
                _getFilterStateCallback.value =
                    Event(GetFilterState.ShowError(it.localizedMessage))
    ,
            GetLimitOrderFilterUseCase.Param(wallet.address)
        )
    }

    fun saveOrderFilter(orderFilter: OrderFilter) {
        saveLimitOrderFilterUseCase.execute(
            Action {
                _saveFilterStateCallback.value = Event(SaveFilterState.Success(""))
    ,
            Consumer {
                it.printStackTrace()
                _saveFilterStateCallback.value =
                    Event(SaveFilterState.ShowError(it.localizedMessage))
    ,
            SaveLimitOrderFilterUseCase.Param(orderFilter)
        )
    }
}