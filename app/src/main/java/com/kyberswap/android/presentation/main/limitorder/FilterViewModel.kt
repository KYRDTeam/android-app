package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrdersFilterSettingUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderFilterUseCase
import com.kyberswap.android.presentation.common.Event
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject

class FilterViewModel @Inject constructor(
    private val saveLimitOrderFilterUseCase: SaveLimitOrderFilterUseCase,
    private val getLimitOrdersFilterSettingUseCase: GetLimitOrdersFilterSettingUseCase
) : ViewModel() {

    private val _getFilterSettingCallback = MutableLiveData<Event<GetFilterSettingState>>()
    val getFilterSettingCallback: LiveData<Event<GetFilterSettingState>>
        get() = _getFilterSettingCallback

    private val _saveFilterStateCallback = MutableLiveData<Event<SaveFilterState>>()
    val saveFilterStateCallback: LiveData<Event<SaveFilterState>>
        get() = _saveFilterStateCallback


    fun getFilterSettings() {

        getLimitOrdersFilterSettingUseCase.execute(
            Consumer {
                _getFilterSettingCallback.value = Event(GetFilterSettingState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getFilterSettingCallback.value =
                    Event(GetFilterSettingState.ShowError(it.localizedMessage))
            },
            null
        )

    }

    fun saveOrderFilter(orderFilter: OrderFilter) {
        saveLimitOrderFilterUseCase.execute(
            Action {
                _saveFilterStateCallback.value = Event(SaveFilterState.Success(""))
            },
            Consumer {
                it.printStackTrace()
                _saveFilterStateCallback.value =
                    Event(SaveFilterState.ShowError(it.localizedMessage))
            },
            SaveLimitOrderFilterUseCase.Param(orderFilter)
        )
    }
}