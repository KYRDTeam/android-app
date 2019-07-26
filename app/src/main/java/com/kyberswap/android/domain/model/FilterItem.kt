package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class FilterItem(
    var isSelected: Boolean = false,
    val name: String = "",
    val displayName: String = ""
) : Parcelable {
    val itemName: String
        get() = if (displayName.isNotEmpty()) displayName else name
}