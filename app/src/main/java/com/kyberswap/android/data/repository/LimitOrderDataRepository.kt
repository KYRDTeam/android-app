package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.LimitOrderApi
import com.kyberswap.android.data.db.LimitOrderDao
import com.kyberswap.android.data.mapper.OrderMapper
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderDataUseCase
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject


class LimitOrderDataRepository @Inject constructor(
    private val limitOrderDao: LimitOrderDao,
    private val limitOrderApi: LimitOrderApi,
    private val orderMapper: OrderMapper
) : LimitOrderRepository {
    override fun getLimitOrders(param: GetLimitOrderDataUseCase.Param): Flowable<Order> {
        return Flowable.mergeDelayError(
            limitOrderDao.findOrderByAddressFlowable(param.walletAddress),
            limitOrderApi.getOrders().toFlowable()
                .map {
                    it.orders
        
                .flatMapIterable { orders ->
                    orders
        
                .map {
                    Timber.e(it.toString())
                    orderMapper.transform(it)
        
        )
    }


}