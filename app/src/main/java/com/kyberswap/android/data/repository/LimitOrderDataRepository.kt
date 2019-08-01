package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.google.common.collect.ImmutableList
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.LimitOrderApi
import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.data.db.*
import com.kyberswap.android.data.mapper.FeeMapper
import com.kyberswap.android.data.mapper.OrderMapper
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.limitorder.*
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.hexWithPrefix
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import org.consenlabs.tokencore.wallet.WalletManager
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LimitOrderDataRepository @Inject constructor(
    private val context: Context,
    private val limitOrderDao: LimitOrderDao,
    private val localLimitOrderDao: LocalLimitOrderDao,
    private val orderFilterDao: OrderFilterDao,
    private val tokenDao: TokenDao,
    private val pendingBalancesDao: PendingBalancesDao,
    private val limitOrderApi: LimitOrderApi,
    private val tokenClient: TokenClient,
    private val orderMapper: OrderMapper,
    private val feeMapper: FeeMapper
) : LimitOrderRepository {

    override fun cancelOrder(param: CancelOrderUseCase.Param): Single<Cancelled> {
        return limitOrderApi.cancelOrder(
            param.order.id
        ).map {
            orderMapper.transform(it)
        }.doAfterSuccess { status ->
            if (status.cancelled) {
                val order = limitOrderDao.findOrderById(param.order.id)
                order?.let {
                    limitOrderDao.updateOrder(it.copy(status = Order.Status.CANCELLED.value))
                }
            }
        }
    }

    override fun saveOrderFilter(param: SaveLimitOrderFilterUseCase.Param): Completable {
        return Completable.fromCallable {
            orderFilterDao.updateOrderFilter(param.orderFilter)
        }
    }

    override fun getOrderFilter(): Flowable<OrderFilter> {
        return Flowable.fromCallable {
            val orderFilter = orderFilterDao.filter
            if (orderFilterDao.filter == null) {

                val default = OrderFilter(
                    unSelectedStatus = listOf(),
                    unSelectedPairs = listOf(),
                    unSelectedAddresses = listOf()
                )
                orderFilterDao.insertOrderFilter(default)
                default
            } else {
                orderFilter
            }
        }.flatMap {
            orderFilterDao.filterFlowable.defaultIfEmpty(it)
        }
    }

    override fun getNonce(param: GetNonceUseCase.Param): Single<String> {
        return limitOrderApi.getNonce(
            param.wallet.address,
            param.limitOrder.tokenSource.tokenAddress,
            param.limitOrder.tokenDest.tokenAddress
        ).map {
            it.nonce
        }
    }

    override fun submitOrder(param: SubmitOrderUseCase.Param): Single<LimitOrderResponse> {
        return Single.fromCallable {
            var password = ""
            if (context is KyberSwapApplication) {
                password = String(
                    context.aead.decrypt(
                        Base64.decode(param.wallet.cipher, Base64.DEFAULT), ByteArray(0)
                    ), Charsets.UTF_8
                )
            }

            val credentials = WalletUtils.loadCredentials(
                password,
                WalletManager.storage.keystoreDir.toString() + "/wallets/" + param.wallet.walletId + ".json"
            )

            val hexString = tokenClient.signOrder(
                param.localLimitOrder,
                credentials,
                context.getString(R.string.limit_order_contract)
            )
            hexString
        }.flatMap { it ->
            limitOrderApi.createOrder(
                param.wallet.address,
                param.localLimitOrder.nonce,
                param.localLimitOrder.tokenSource.tokenAddress,
                param.localLimitOrder.tokenDest.tokenAddress,
                param.localLimitOrder.sourceAmountWithPrecision.toString(16).hexWithPrefix(),
                param.localLimitOrder.minRateWithPrecision.toString(16).hexWithPrefix(),
                param.wallet.address,
                param.localLimitOrder.feeAmountWithPrecision.toString(16).hexWithPrefix(),
                it
            ).map {
                orderMapper.transform(it)
            }.doAfterSuccess {
                if (it.success) {
                    limitOrderDao.insertOrder(it.order)
                }
            }
        }


    }

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
        val wallet = param.wallet
        val defaultLimitOrder = when (val limitOrder =
            localLimitOrderDao.findLocalLimitOrderByAddress(wallet.address)) {
            null -> {
                val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
                val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)

                val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO
                val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

                val defaultSourceToken =
                    wethToken?.updateBalance(ethBalance.plus(wethBalance))?.copy(
                        tokenSymbol = Token.ETH_SYMBOL_STAR
                    )

                val defaultDestToken = tokenDao.getTokenBySymbol(Token.KNC)

                val order = LocalLimitOrder(
                    wallet.address,
                    defaultSourceToken ?: Token(),
                    defaultDestToken ?: Token()
                )

                localLimitOrderDao.insertOrder(order)
                order
            }
            else -> when {
                limitOrder.tokenSource.selectedWalletAddress != wallet.address -> {
                    val source = limitOrder.tokenSource.updateSelectedWallet(wallet)
                    val dest = limitOrder.tokenDest.updateSelectedWallet(wallet)
                    limitOrder.copy(tokenSource = source, tokenDest = dest)
                }
                else -> limitOrder
            }
        }


        val source = updateBalance(defaultLimitOrder.tokenSource, wallet)
        val dest = updateBalance(defaultLimitOrder.tokenDest, wallet)

        val order = when {
            source.isETHWETH -> {
                val ethToken =
                    tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
                val wethToken =
                    tokenDao.getTokenBySymbol(Token.WETH_SYMBOL) ?: Token()

                defaultLimitOrder.copy(
                    ethToken = ethToken,
                    wethToken = wethToken
                )
            }
            else -> defaultLimitOrder
        }

        val orderWithToken = order.copy(
            tokenSource = source,
            tokenDest = dest
        )

        localLimitOrderDao.insertOrder(orderWithToken)

        return localLimitOrderDao.findLocalLimitOrderByAddressFlowable(wallet.address)
            .defaultIfEmpty(orderWithToken)
    }

    private fun <T> zipWithFlatMap(): FlowableTransformer<T, Long> {
        return FlowableTransformer { flowable ->
            flowable.zipWith(
                Flowable.range(COUNTER_START, ATTEMPTS),
                BiFunction<T, Int, Int> { _: T, u: Int -> u })
                .flatMap { t -> Flowable.timer(t * 5L, TimeUnit.SECONDS) }
        }
    }

    private fun updateBalance(token: Token, wallet: Wallet): Token {
        return when {
            token.isETHWETH -> {

                val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
                val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)
                val ethBalance = if (ethToken?.selectedWalletAddress != wallet.address) {
                    ethToken?.updateSelectedWallet(wallet)
                } else {
                    ethToken
                }?.currentBalance ?: BigDecimal.ZERO

                val wethBalance = if (wethToken?.selectedWalletAddress != wallet.address) {
                    wethToken?.updateSelectedWallet(wallet)
                } else {
                    wethToken
                }?.currentBalance ?: BigDecimal.ZERO

                token.updateBalance(
                    ethBalance.plus(wethBalance)
                )
            }
            else -> {
                when {
                    token.selectedWalletAddress != wallet.address -> token.updateSelectedWallet(
                        wallet
                    )
                    else -> token
                }
            }
        }
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

            val tokenPairUnChanged = if (param.isSourceToken) {
                currentLimitOrderForWalletAddress?.tokenSource?.tokenSymbol == param.token.tokenSymbol
            } else {
                currentLimitOrderForWalletAddress?.tokenDest?.tokenSymbol == param.token.tokenSymbol
            }

            val resetRate = if (!tokenPairUnChanged) {
                ""
            } else {
                currentLimitOrderForWalletAddress?.expectedRate ?: ""
            }


            val order = if (param.isSourceToken) {
                currentLimitOrderForWalletAddress?.copy(
                    tokenSource = param.token,
                    expectedRate = resetRate
                )
            } else {
                currentLimitOrderForWalletAddress?.copy(
                    tokenDest = param.token,
                    expectedRate = resetRate
                )
            }
            order?.let { localLimitOrderDao.updateOrder(it) }
        }
    }

    override fun getLimitOrders(): Flowable<List<Order>> {

        return Flowable.range(1, Integer.MAX_VALUE)
            .concatMap {
                limitOrderApi.getOrders(it).toFlowable()
            }
            .takeUntil {
                it.pagingInfo.pageIndex > it.pagingInfo.pageCount
            }.reduce(
                ImmutableList.builder<OrderEntity>(),
                { builder, response -> builder.addAll(response.orders) })
            .map {
                it.build()
            }
            .map {
                orderMapper.transform(it)
            }
            .repeatWhen {
                it.delay(10, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun getRelatedLimitOrders(param: GetRelatedLimitOrdersUseCase.Param): Flowable<List<Order>> {
        return limitOrderApi.getRelatedOrders(
            param.walletAddress,
            param.tokenSource.tokenAddress,
            param.tokenDest.tokenAddress,
            "0"
        )
            .map {
                it.orders
            }
            .map {
                orderMapper.transform(it)
            }
            .toFlowable()
            .repeatWhen {
                it.delay(10, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }

    }

    override fun getPendingBalances(param: GetPendingBalancesUseCase.Param): Flowable<PendingBalances> {
        return Flowable.mergeDelayError(
            pendingBalancesDao.pendingBalancesByWalletAddress(param.wallet.address),
            limitOrderApi.getPendingBalances(param.wallet.address)
                .map {
                    orderMapper.transform(it)
                }.doAfterSuccess {
                    pendingBalancesDao.createNewPendingBalances(it.copy(walletAddress = param.wallet.address))
                }.toFlowable()
                .repeatWhen {
                    it.delay(5, TimeUnit.SECONDS)
                }
                .retryWhen { throwable ->
                    throwable.compose(zipWithFlatMap())
                }

        )
    }

    companion object {
        private const val COUNTER_START = 1
        private const val ATTEMPTS = 5
    }
}