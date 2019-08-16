package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.db.ListStringConverter
import com.kyberswap.android.data.db.TokenPairTypeConverter
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "order_filter")
@Parcelize
data class OrderFilter(
    @PrimaryKey
    var walletAddress: String = "",
    var oldest: Boolean = false,
    @TypeConverters(TokenPairTypeConverter::class)
    var unSelectedPairs: List<Pair<String, String>> = listOf(),
    @TypeConverters(ListStringConverter::class)
    var unSelectedAddresses: List<String> = listOf(),
    @TypeConverters(ListStringConverter::class)
    var unSelectedStatus: List<String> = listOf()
) : Parcelable {

    companion object {
        const val TOKEN_PAIR_SEPARATOR = " ➞ "
    }
}

