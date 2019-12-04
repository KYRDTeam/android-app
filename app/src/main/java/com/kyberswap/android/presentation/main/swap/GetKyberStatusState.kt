package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.KyberEnabled

sealed class GetKyberStatusState {
    object Loading : GetKyberStatusState()
    class ShowError(val message: String?) : GetKyberStatusState()
    class Success(val kyberEnabled: KyberEnabled) : GetKyberStatusState()
}
