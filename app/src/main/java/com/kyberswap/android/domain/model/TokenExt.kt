package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "token_extras")
@Parcelize
data class TokenExt(
    val tokenSymbol: String = "",
    val tokenName: String = "",
    @NonNull
    @PrimaryKey
    val tokenAddress: String = "",
    val isGasFixed: Boolean = false,
    val gasLimit: String = "",
    var delistTime: Long = 0
) : Parcelable {
    constructor(token: Token) : this(
        token.tokenSymbol,
        token.tokenName,
        token.tokenAddress,
        token.isGasFixed,
        token.gasLimit,
        token.delistTime
    )
}