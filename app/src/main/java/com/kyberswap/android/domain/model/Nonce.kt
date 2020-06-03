package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.math.BigInteger

@Entity(tableName = "nonces")
@Parcelize
data class Nonce(
    @PrimaryKey
    val walletAddress: String,
    val nonce: BigInteger = BigInteger.ZERO,
    val hash: String? = ""
) : Parcelable