package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.LimitOrderApi
import com.kyberswap.android.data.db.LimitOrderDao
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.mapper.FeeMapper
import com.kyberswap.android.data.mapper.OrderMapper
import com.kyberswap.android.domain.model.Fee
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.limitorder.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject


class LimitOrderDataRepository @Inject constructor(
    private val limitOrderDao: LimitOrderDao,
    private val localLimitOrderDao: LocalLimitOrderDao,
    private val tokenDao: TokenDao,
    private val limitOrderApi: LimitOrderApi,
    private val orderMapper: OrderMapper,
    private val feeMapper: FeeMapper
) : LimitOrderRepository {

    override fun getLimitOrderFee(param: GetLimitOrderFeeUseCase.Param): Single<Fee> {
        return limitOrderApi.getFee(
            param.sourceToken.tokenAddress,
            param.destToken.tokenAddress,
            param.sourceAmount,
            param.destAmount,
            param.userAddress
        ).map {
            feeMapper.transform(it)
        }

    }

    override fun getCurrentLimitOrders(param: GetLocalLimitOrderDataUseCase.Param): Flowable<LocalLimitOrder> {
        val limitOrder = localLimitOrderDao.findLocalLimitOrderByAddress(param.walletAddress)
        val defaultLimitOrder = if (limitOrder == null) {
            val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
            val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)

            val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO
            val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

            val defaultSourceToken = ethToken?.copy(
                tokenSymbol = Token.ETH_SYMBOL_STAR,
                currentBalance = (ethBalance.plus(wethBalance))
            )

            val defaultDestToken = tokenDao.getTokenBySymbol(Token.KNC)

            val order = LocalLimitOrder(
                param.walletAddress,
                defaultSourceToken ?: Token(),
                defaultDestToken ?: Token()
            )

            localLimitOrderDao.insertOrder(order)
            order
        } else {
            limitOrder
        }

        return localLimitOrderDao.findLocalLimitOrderByAddressFlowable(param.walletAddress)
            .defaultIfEmpty(
                defaultLimitOrder
            )
    }

    override fun saveLimitOrder(param: SaveLimitOrderUseCase.Param): Completable {
        return Completable.fromCallable {
            localLimitOrderDao.insertOrder(param.order)
        }
    }

    override fun saveLimitOrder(param: SaveLimitOrderTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val currentLimitOrderForWalletAddress =
                localLimitOrderDao.findLocalLimitOrderByAddress(param.walletAddress)
            val order = if (param.isSourceToken) {
                currentLimitOrderForWalletAddress?.copy(tokenSource = param.token)
            } else {
                currentLimitOrderForWalletAddress?.copy(tokenDest = param.token)
            }
            order?.let { localLimitOrderDao.updateOrder(it) }
        }
    }

    override fun getLimitOrders(param: GetLimitOrdersUseCase.Param): Flowable<List<Order>> {
        return limitOrderApi.getOrders()
            .map {
                it.orders
            }
            .map {
                orderMapper.transform(it)
            }
            .doAfterSuccess {
                limitOrderDao.insertOrders(it)
            }.toFlowable()
    }

    override fun getRelatedLimitOrders(param: GetRelatedLimitOrdersUseCase.Param): Flowable<List<Order>> {
        return limitOrderApi.getRelatedOrders(
            param.walletAddress,
            param.srcToken,
            param.dstToken,
            param.status
        )
            .map {
                it.orders
            }
            .map {
                orderMapper.transform(it)
            }
            .doAfterSuccess {
                limitOrderDao.insertOrders(it)
            }.toFlowable()

    }


}