package com.kyberswap.android.domain.model

import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero

@Entity(tableName = "swaps")
data class Swap(
    @NonNull
    @PrimaryKey
    val walletAddress: String = "",
    @Embedded(prefix = "source_")
    var tokenSource: Token = Token(),
    @Embedded(prefix = "dest_")
    var tokenDest: Token = Token(),
    var sourceAmount: String = "",
    var destAmount: String = "",
    var expectedRate: String = "",
    var slippageRate: String = ""

) {

    val displayExpectedRate: String
        get() = expectedRate.toBigDecimalOrDefaultZero().toPlainString()

    fun switch(): Swap {
        return Swap(
            this.walletAddress,
            this.tokenDest,
            this.tokenSource,
            this.destAmount,
            this.sourceAmount,
            this.expectedRate,
            this.slippageRate

        )
    }
}