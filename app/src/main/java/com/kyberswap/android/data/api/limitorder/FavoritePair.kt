package com.kyberswap.android.data.api.limitorder

import com.google.gson.annotations.SerializedName
import com.kyberswap.android.domain.model.Token

data class FavoritePair(
    @SerializedName("base")
    val base: String = "",
    @SerializedName("quote")
    val quote: String = ""
) {
    val displayBase: String
        get() = if (base.equals(Token.WETH_SYMBOL, true) || base.equals(
                Token.ETH_SYMBOL,
                true
            )
        ) Token.ETH_SYMBOL_STAR else base

    val displayQuote: String
        get() = if (quote.equals(Token.WETH_SYMBOL, true) || quote.equals(
                Token.ETH_SYMBOL,
                true
            )
        ) Token.ETH_SYMBOL_STAR else quote

    val displayPair: String
        get() = displayQuote + "_" + displayBase
}