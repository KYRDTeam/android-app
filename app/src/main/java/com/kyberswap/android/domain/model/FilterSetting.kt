package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class FilterSetting(
    val pairs: List<FilterItem> = listOf(),
    val status: List<FilterItem> = listOf(),
    val address: List<FilterItem> = listOf(),
    val asc: Boolean = false,
    val orderFilter: OrderFilter = OrderFilter()
) : Parcelable {

    companion object {
        val DEFAULT_ORDER_STATUS by lazy {
            listOf(
                Order.Status.OPEN,
                Order.Status.IN_PROGRESS,
                Order.Status.FILLED,
                Order.Status.CANCELLED,
                Order.Status.INVALIDATED
            ).map {
                it.value
            }
        }
    }
}