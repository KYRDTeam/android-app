package com.kyberswap.android.util.ext

import com.kyberswap.android.BuildConfig
import com.kyberswap.android.data.repository.WalletDataRepository
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction

fun WCEthereumTransaction.isSwapTx(): Boolean {
    return if (BuildConfig.FLAVOR == "dev") {
        this.data.take(10) == WalletDataRepository.METHOD_ID_TRADE_WITH_HINT_AND_FEE
    } else {
        this.data.take(10) == WalletDataRepository.METHOD_ID_SWAP
    }
}

fun WCEthereumTransaction.isTransferETHTx(): Boolean {
    return this.data.isEmpty()
}

fun WCEthereumTransaction.isTransferTokenTx(): Boolean {
    return this.data.take(10) == WalletDataRepository.METHOD_ID_TRANSFER
}

fun WCEthereumTransaction.isApproveTx(): Boolean {
    return this.data.take(10) == WalletDataRepository.METHOD_ID_APPROVE
}

fun WCEthereumTransaction.isFromKyberSwap(): Boolean {
    return isSwapTx() || isTransferETHTx() || isTransferTokenTx() || isApproveTx()
}