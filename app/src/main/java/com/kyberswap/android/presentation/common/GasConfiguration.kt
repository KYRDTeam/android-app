package com.kyberswap.android.presentation.common

import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import java.math.BigInteger

fun calculateDefaultGasLimit(from: Token, to: Token): BigInteger {
    if (from == to) {
        // normal transfer
        if (from.isETH) {
            return Token.TRANSFER_ETH_GAS_LIMIT_DEFAULT.toBigInteger()
        } else {
            return calculateDefaultGasLimitTransfer(from)
        }
    }
    val gasSrcToETH =
        if (from.gasLimit.toBigIntegerOrDefaultZero() > BigInteger.ZERO) {
            from.gasLimit.toBigIntegerOrDefaultZero()
        } else if (from.isETH) {
            BigInteger.ZERO
        } else if (from.isDGX) {
            Token.DIGIX_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (from.isDAI) {
            Token.DAI_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (from.isMKR) {
            Token.MAKER_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (from.isPRO) {
            Token.PROPY_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (from.isPT) {
            Token.PROMOTION_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (from.isTUSD) {
            Token.TRUE_USD_GAS_LIMIT_DEFAULT.toBigInteger()
        } else {
            Token.EXCHANGE_ETH_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
        }


    val gasETHToDest =
        if (to.gasLimit.toBigIntegerOrDefaultZero() > BigInteger.ZERO) {
            to.gasLimit.toBigIntegerOrDefaultZero()
        } else if (to.isETH) {
            BigInteger.ZERO
        } else if (to.isDGX) {
            Token.DIGIX_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (to.isDAI) {
            Token.DAI_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (to.isMKR) {
            Token.MAKER_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (to.isPRO) {
            Token.PROPY_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (to.isPT) {
            Token.PROMOTION_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (to.isTUSD) {
            Token.TRUE_USD_GAS_LIMIT_DEFAULT.toBigInteger()
        } else {
            Token.EXCHANGE_ETH_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
        }

    return gasSrcToETH + gasETHToDest
}


fun specialGasLimitDefault(from: Token, to: Token): BigInteger? {
    if (from.isDAI || from.isTUSD || to.isDAI || to.isTUSD) {
        return calculateDefaultGasLimit(from, to)
    }
    return null
}


fun calculateDefaultGasLimitTransfer(token: Token): BigInteger {
    val gasDefault: BigInteger = {
        if (token.gasLimit.toBigIntegerOrDefaultZero() > BigInteger.ZERO) {
            token.gasLimit.toBigIntegerOrDefaultZero()
        } else if (token.isETH) {
            Token.TRANSFER_ETH_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (token.isDGX) {
            Token.DIGIX_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (token.isDAI) {
            Token.DAI_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (token.isMKR) {
            Token.MAKER_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (token.isPRO) {
            Token.PROPY_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (token.isPT) {
            Token.PROMOTION_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
        } else if (token.isTUSD) {
            Token.TRUE_USD_GAS_LIMIT_DEFAULT.toBigInteger()
        } else {
            Token.TRANSFER_TOKEN_GAS_LIMIT_DEFAULT.toBigInteger()
        }

    }()
    return gasDefault * 120.toBigInteger() / 100.toBigInteger()
}