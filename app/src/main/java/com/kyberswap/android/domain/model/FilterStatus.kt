package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class FilterStatus(
    val isSelected: Boolean = false,
    val name: String = "",
    val status: String
) : Parcelable