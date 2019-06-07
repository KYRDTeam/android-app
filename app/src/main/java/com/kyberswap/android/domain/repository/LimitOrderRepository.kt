package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Fee
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.usecase.limitorder.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface LimitOrderRepository {

    fun getLimitOrders(param: GetLimitOrdersUseCase.Param): Flowable<List<Order>>

    fun getRelatedLimitOrders(param: GetRelatedLimitOrdersUseCase.Param): Flowable<List<Order>>

    fun getCurrentLimitOrders(param: GetLocalLimitOrderDataUseCase.Param): Flowable<LocalLimitOrder>

    fun saveLimitOrder(param: SaveLimitOrderTokenUseCase.Param): Completable

    fun saveLimitOrder(param: SaveLimitOrderUseCase.Param): Completable

    fun getLimitOrderFee(param: GetLimitOrderFeeUseCase.Param): Single<Fee>
}