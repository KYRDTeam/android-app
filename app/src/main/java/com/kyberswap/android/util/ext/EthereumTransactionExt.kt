package com.kyberswap.android.util.ext

import com.kyberswap.android.BuildConfig
import com.kyberswap.android.data.repository.WalletDataRepository
import org.web3j.protocol.core.methods.response.Transaction
import java.math.BigInteger

fun Transaction.isSwapTx(): Boolean {
    return if (BuildConfig.FLAVOR == "dev") {
        this.input.take(10) == WalletDataRepository.METHOD_ID_TRADE_WITH_HINT_AND_FEE
    } else {
        this.input.take(10) == WalletDataRepository.METHOD_ID_SWAP
    }
}

fun Transaction.isTransferETHTx(): Boolean {
    return this.input.isEmpty() || this.input.equals("0x", true)
}

fun Transaction.isTransferTokenTx(): Boolean {
    return this.input.take(10) == WalletDataRepository.METHOD_ID_TRANSFER
}

fun Transaction.isApproveTx(): Boolean {
    return this.input.take(10) == WalletDataRepository.METHOD_ID_APPROVE
}

fun Transaction.isFromKyberSwap(): Boolean {
    return isSwapTx() || isTransferETHTx() || isTransferTokenTx() || isApproveTx()
}

fun Transaction.fromAddress(list: List<String>): String {
    return list[WalletDataRepository.TRADE_WITH_HINT_SOURCE_POSITION].takeLast(
        40
    ).toWalletAddress()
}

fun Transaction.toAddress(list: List<String>): String {
    return list[WalletDataRepository.TRADE_WITH_HINT_DEST_POSITION].takeLast(40).toWalletAddress()
}

fun Transaction.txValue(list: List<String>): BigInteger {
    return list[WalletDataRepository.TRADE_WITH_HINT_SOURCE_AMOUNT_POSITION].toBigIntSafe()
}


fun Transaction.minConversionRate(list: List<String>): BigInteger {
    return list[WalletDataRepository.TRADE_WITH_HINT_MIN_CONVERSION_RATE_POSITION].toBigIntSafe()
}

fun Transaction.platformFee(list: List<String>): BigInteger {
    return list[WalletDataRepository.TRADE_WITH_HINT_AND_FEE_PLATFORM_FEE_POSITION].toBigIntSafe()
}

fun Transaction.transferAmount(params: List<String>): BigInteger {
    return if (this.isTransferETHTx()) {
        this.value
    } else {
        params[WalletDataRepository.TRANFER_TOKEN_AMOUNT_POSITION].toBigIntSafe()
    }
}

fun Transaction.transferToAddress(params: List<String>): String {
    return params[WalletDataRepository.TRANFER_TOKEN_ADDRESS_POSITION].takeLast(
        40
    ).toWalletAddress()
}


fun Transaction.params(): List<String> {
    val data = this.input
    val methodId = data.take(10)
    return data.removePrefix(methodId).chunked(64)
}