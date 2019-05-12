package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.Cap

sealed class GetCapState {
    object Loading : GetCapState()
    class ShowError(val message: String?) : GetCapState()
    class Success(val cap: Cap) : GetCapState()
}
