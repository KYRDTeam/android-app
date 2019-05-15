package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.presentation.common.DEFAULT_ROUNDING_NUMBER
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import kotlinx.android.parcel.Parcelize
import java.math.RoundingMode

@Entity(tableName = "swaps")
@Parcelize
data class Swap(
    @NonNull
    @PrimaryKey
    val walletAddress: String = "",
    @Embedded(prefix = "source_")
    val tokenSource: Token = Token(),
    @Embedded(prefix = "dest_")
    val tokenDest: Token = Token(),
    var sourceAmount: String = "",
    var destAmount: String = "",
    var expectedRate: String = "",
    var slippageRate: String = "",
    var gasPrice: String = "",
    var gasLimit: String = "",
    var marketRate: String = ""

) : Parcelable {
    val displayExpectedRate: String
        get() = expectedRate.toBigDecimalOrDefaultZero()
            .setScale(DEFAULT_ROUNDING_NUMBER, RoundingMode.UP)
            .toPlainString()

    fun swapToken(): Swap {
        return Swap(
            this.walletAddress,
            this.tokenDest,
            this.tokenSource,
            this.destAmount,
            this.sourceAmount
//            this.expectedRate,
//            this.slippageRate,
//            this.gasPrice,
//            this.gasLimit,
//            this.percentageRate

        )
    }


}