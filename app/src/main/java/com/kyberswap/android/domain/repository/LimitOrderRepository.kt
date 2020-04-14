package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Cancelled
import com.kyberswap.android.domain.model.EligibleAddress
import com.kyberswap.android.domain.model.Fee
import com.kyberswap.android.domain.model.LimitOrderResponse
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.SelectedMarketItem
import com.kyberswap.android.domain.usecase.limitorder.CancelOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.CheckEligibleAddressUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFeeUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLocalLimitOrderDataUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetMarketUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetNonceUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetPendingBalancesUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetRelatedLimitOrdersUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetSelectedMarketUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderFilterUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderTokenUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveLimitOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveMarketItemUseCase
import com.kyberswap.android.domain.usecase.limitorder.SaveSelectedMarketUseCase
import com.kyberswap.android.domain.usecase.limitorder.SubmitOrderUseCase
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

    fun pollingMarket(): Flowable<List<MarketItem>>

    fun getStableQuoteTokens(): Single<List<String>>

    fun getMarkets(forceRefresh: Boolean): Flowable<List<MarketItem>>

    fun saveMarketIem(param: SaveMarketItemUseCase.Param): Completable

    fun saveSelectedMarket(param: SaveSelectedMarketUseCase.Param): Completable

    fun getSelectedMarket(param: GetSelectedMarketUseCase.Param): Flowable<SelectedMarketItem>

    fun getMarket(param: GetMarketUseCase.Param): Flowable<MarketItem>
}