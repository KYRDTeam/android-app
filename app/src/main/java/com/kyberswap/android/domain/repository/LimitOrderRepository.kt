package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderDataUseCase
import io.reactivex.Flowable

interface LimitOrderRepository {

    fun getLimitOrders(param: GetLimitOrderDataUseCase.Param): Flowable<Order>
}