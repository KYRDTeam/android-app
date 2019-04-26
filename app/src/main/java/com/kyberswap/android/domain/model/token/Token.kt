package com.kyberswap.android.domain.model.token

import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.util.ext.toDisplayNumber
import java.math.BigDecimal

data class Token(
    val timestamp: Long = 0,
    val tokenSymbol: String = "",
    val tokenName: String = "",
    val tokenAddress: String = "",
    val tokenDecimal: Int = 0,
    var rateEthNow: BigDecimal = BigDecimal.ZERO,
    val changeEth24h: BigDecimal = BigDecimal.ZERO,
    var rateUsdNow: BigDecimal = BigDecimal.ZERO,
    val changeUsd24h: BigDecimal = BigDecimal.ZERO
) {
    constructor(entity: TokenEntity) : this(
        entity.timestamp,
        entity.tokenSymbol,
        entity.tokenName,
        entity.tokenAddress,
        entity.tokenDecimal,
        entity.rateEthNow,
        entity.changeEth24h,
        entity.rateUsdNow,
        entity.changeUsd24h
    )

    val displayRateEthNow: String
        get() = rateEthNow.toDisplayNumber()
    val displayChangeEth24h: String
        get() = changeEth24h.toDisplayNumber()
    val displayRateUsdNow: String
        get() = rateUsdNow.toDisplayNumber()
    val displayChangeUsdNow: String
        get() = changeUsd24h.toDisplayNumber()

    val displayCurrentBalance: String
        get() = currentBalance.toDisplayNumber()

    var currentBalance: BigDecimal = BigDecimal.ZERO

    fun isETH(): Boolean {
        return tokenSymbol.toLowerCase() == ETH_SYMBOL.toLowerCase()
    }

    companion object {
        const val ETH_SYMBOL = "ETH"
    }
}