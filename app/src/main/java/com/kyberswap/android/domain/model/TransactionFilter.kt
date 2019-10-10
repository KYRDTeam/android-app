package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.db.ListStringConverter
import com.kyberswap.android.data.db.TransactionTypesConverter
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "transaction_filter")
@Parcelize
data class TransactionFilter(
    @PrimaryKey
    val walletAddress: String = "",
    val from: String = "",
    val to: String = "",
    @TypeConverters(TransactionTypesConverter::class)
    val types: List<Transaction.TransactionType> = listOf(),
    @TypeConverters(ListStringConverter::class)
    val tokens: List<String> = listOf()
) : Parcelable