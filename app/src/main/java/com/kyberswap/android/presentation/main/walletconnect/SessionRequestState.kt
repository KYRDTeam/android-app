package com.kyberswap.android.presentation.main.walletconnect

import com.trustwallet.walletconnect.models.WCPeerMeta

sealed class SessionRequestState {
    object Loading : SessionRequestState()
    class ShowError(val message: String?) : SessionRequestState()
    class Success(val status: Boolean = false, val meta: WCPeerMeta) : SessionRequestState()
}
