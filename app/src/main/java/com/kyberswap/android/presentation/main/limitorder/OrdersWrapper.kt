package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.Order


data class OrdersWrapper(val orders: List<Order>, val asc: Boolean)