package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLocalLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderTokenUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderUseCase
import io.reactivex.Completable
import io.reactivex.Flowable

interface LimitOrderRepository {

    fun getLimitOrders(param: GetLimitOrderDataUseCase.Param): Flowable<Order>

    fun getCurrentLimitOrders(param: GetLocalLimitOrderDataUseCase.Param): Flowable<LocalLimitOrder>

    fun saveLimitOrder(param: SaveLimitOrderTokenUseCase.Param): Completable

    fun saveLimitOrder(param: SaveLimitOrderUseCase.Param): Completable
}