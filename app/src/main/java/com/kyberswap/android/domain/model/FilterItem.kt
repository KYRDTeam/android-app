package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class FilterItem(var isSelected: Boolean = false, val name: String = "") : Parcelable