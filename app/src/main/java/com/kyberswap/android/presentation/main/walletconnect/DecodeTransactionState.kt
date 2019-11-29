package com.kyberswap.android.presentation.main.walletconnect

import com.kyberswap.android.domain.model.Transaction
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction

sealed class DecodeTransactionState {
    object Loading : DecodeTransactionState()
    class ShowError(val message: String?) : DecodeTransactionState()
    class Success(
        val id: Long,
        val wcTransaction: WCEthereumTransaction,
        val transaction: Transaction
    ) : DecodeTransactionState()
}
