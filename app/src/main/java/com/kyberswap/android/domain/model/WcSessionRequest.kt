package com.kyberswap.android.domain.model

import com.trustwallet.walletconnect.models.WCPeerMeta

data class WcSessionRequest(
    val status: Boolean,
    val meta: WCPeerMeta
)