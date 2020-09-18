package com.kyberswap.android.domain.model

import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage

data class WcEthSign(
    val id: Long,
    val message: WCEthereumSignMessage
)