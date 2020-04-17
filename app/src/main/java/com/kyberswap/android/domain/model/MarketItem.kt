package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.limitorder.MarketItemEntity
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Entity(tableName = "markets")
@Parcelize
data class MarketItem(
    @SerializedName("buy_price")
    val buyPrice: String = "",
    @SerializedName("change")
    val change: String = "",
    @PrimaryKey
    @SerializedName("pair")
    val pair: String = "",
    @SerializedName("sell_price")
    val sellPrice: String = "",
    @SerializedName("volume")
    val volume: String = "",
    var isFav: Boolean = false
) : Parcelable {

    val combinedPair: String
        get() {
            return if (pair.contains(Token.ETH_SYMBOL, true)) {
                if (pair.contains(Token.WETH_SYMBOL, true)) {
                    pair.replace(Token.WETH_SYMBOL, Token.ETH_SYMBOL_STAR)
                } else {
                    pair.replace(Token.ETH_SYMBOL, Token.ETH_SYMBOL_STAR)
                }
            } else {
                pair
            }
        }

    val displayMarketVol: String
        get() = "$displayVolume $quoteSymbol"

    constructor(entity: MarketItemEntity) : this(
        entity.buyPrice, entity.change, entity.pair, entity.sellPrice, entity.volume
    )

    val chartMarket: String
        get() = (baseSymbol + "_" + quoteSymbol).replace("*", "")

    val baseSymbol: String
        get() = pair.split("_").last()

    val quoteSymbol: String
        get() = pair.split("_").first()

    val displayPair: String
        get() {
            return try {
                val list = pair.split("_")
                list.last() + "/" + list.first()
            } catch (e: Exception) {
                e.printStackTrace()
                pair
            }
        }

    val displayBuyPrice: String
        get() {
            val bp = buyPrice.toBigDecimalOrDefaultZero()
            return if (bp == BigDecimal.ZERO) {
                "--"
            } else {
                bp.toDisplayNumber()
            }
        }

    val displaySellPrice: String
        get() {
            val sp = sellPrice.toBigDecimalOrDefaultZero()
            return if (sp == BigDecimal.ZERO) {
                "--"
            } else {
                sp.toDisplayNumber()
            }
        }

    val displayVolume: String
        get() {
            val vl = volume.toBigDecimalOrDefaultZero()
            return if (vl == BigDecimal.ZERO) {
                "--"
            } else {
                vl.toDisplayNumber()
            }
        }

    companion object {
        const val DEFAULT_PAIR = "ETH*_KNC"
    }
}