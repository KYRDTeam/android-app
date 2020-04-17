package com.kyberswap.android.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_markets")
data class SelectedMarketItem(
    @PrimaryKey
    val walletAddress: String = "",
    val pair: String = ""
) {

    constructor(walletAddress: String) : this(walletAddress, MarketItem.DEFAULT_PAIR)

    val base: String
        get() {
            return try {
                pair.split("_").last()
            } catch (e: Exception) {
                e.printStackTrace()
                Token.KNC
            }
        }

    val quote: String
        get() {
            return try {
                pair.split("_").first()
            } catch (e: Exception) {
                e.printStackTrace()
                Token.ETH_SYMBOL_STAR
            }
        }
}