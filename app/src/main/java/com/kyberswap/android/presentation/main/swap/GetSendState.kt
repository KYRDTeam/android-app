package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.Send

sealed class GetSendState {
    object Loading : GetSendState()
    class ShowError(val message: String?) : GetSendState()
    class Success(val send: Send) : GetSendState()
}
