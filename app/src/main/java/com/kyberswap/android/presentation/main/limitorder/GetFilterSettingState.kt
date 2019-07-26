package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.FilterSetting

sealed class GetFilterSettingState {
    object Loading : GetFilterSettingState()
    class ShowError(val message: String?) : GetFilterSettingState()
    class Success(val filterSetting: FilterSetting) : GetFilterSettingState()
}
