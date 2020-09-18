package com.kyberswap.android.domain.model

import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction

data class WcEthSendTransaction(
    val id: Long,
    val wcTransaction: WCEthereumTransaction,
    val transaction: Transaction
)