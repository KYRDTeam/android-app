package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.*
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

    fun submitOrder(param: SubmitOrderUseCase.Param): Single<LimitOrderResponse>

    fun getNonce(param: GetNonceUseCase.Param): Single<String>

    fun getOrderFilter(param: GetLimitOrderFilterUseCase.Param): Flowable<OrderFilter>

    fun saveOrderFilter(param: SaveLimitOrderFilterUseCase.Param): Completable

    fun cancelOrder(param: CancelOrderUseCase.Param): Single<Cancelled>
}