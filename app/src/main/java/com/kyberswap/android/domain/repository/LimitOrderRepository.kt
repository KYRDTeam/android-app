package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.usecase.limitorder.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface LimitOrderRepository {

    fun getLimitOrders(): Flowable<List<Order>>

    fun getPendingBalances(param: GetPendingBalancesUseCase.Param): Flowable<PendingBalances>

    fun getRelatedLimitOrders(param: GetRelatedLimitOrdersUseCase.Param): Flowable<List<Order>>

    fun getCurrentLimitOrders(param: GetLocalLimitOrderDataUseCase.Param): Flowable<LocalLimitOrder>

    fun saveLimitOrder(param: SaveLimitOrderTokenUseCase.Param): Completable

    fun saveLimitOrder(param: SaveLimitOrderUseCase.Param): Completable

    fun getLimitOrderFee(param: GetLimitOrderFeeUseCase.Param): Flowable<Fee>

    fun submitOrder(param: SubmitOrderUseCase.Param): Single<LimitOrderResponse>

    fun getNonce(param: GetNonceUseCase.Param): Single<String>

    fun eligibleAddress(param: CheckEligibleAddressUseCase.Param): Single<EligibleAddress>

    fun getOrderFilter(): Flowable<OrderFilter>

    fun saveOrderFilter(param: SaveLimitOrderFilterUseCase.Param): Completable

    fun cancelOrder(param: CancelOrderUseCase.Param): Single<Cancelled>

}