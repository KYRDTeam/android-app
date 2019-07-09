package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "pass_codes"
)
data class PassCode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val digest: String = ""
) : Parcelable