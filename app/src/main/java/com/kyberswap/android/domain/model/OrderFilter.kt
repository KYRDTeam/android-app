package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
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
    var pairs: List<Pair<String, String>> = listOf(),
    @TypeConverters(ListStringConverter::class)
    var addresses: List<String> = listOf(),
    @TypeConverters(ListStringConverter::class)
    var status: List<String> = listOf(),
    @Ignore
    var listOrders: List<FilterItem> = listOf(),
    @Ignore
    var listAddress: List<FilterItem> = listOf(),
    @Ignore
    var listStatus: List<FilterItem> = listOf()
) : Parcelable