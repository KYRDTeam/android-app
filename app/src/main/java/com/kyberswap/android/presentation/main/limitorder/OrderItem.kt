package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.Order


sealed class OrderItem {
    class Header(val date: String?) : OrderItem()
    class ItemEven(val order: Order) : OrderItem()
    class ItemOdd(val order: Order) : OrderItem()
}