package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.LimitOrderApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.api.limitorder.FavoritePair
import com.kyberswap.android.data.api.limitorder.FavoritePairStatus
import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.data.db.LimitOrderDao
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.MarketDao
import com.kyberswap.android.data.db.OrderFilterDao
import com.kyberswap.android.data.db.PendingBalancesDao
import com.kyberswap.android.data.db.SelectedMarketDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TokenExtDao
import com.kyberswap.android.data.mapper.FeeMapper
import com.kyberswap.android.data.mapper.OrderMapper
import com.kyberswap.android.domain.model.Cancelled
import com.kyberswap.android.domain.model.EligibleAddress
import com.kyberswap.android.domain.model.Fee
import com.kyberswap.android.domain.model.LimitOrderResponse
import com.kyberswap.android.domain.model.LocalLimitOrder
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.SelectedMarketItem
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.LimitOrderRepository
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
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.hexWithPrefix
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import org.consenlabs.tokencore.wallet.WalletManager
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LimitOrderDataRepository @Inject constructor(
    private val context: Context,
    private val limitOrderDao: LimitOrderDao,
    private val localLimitOrderDao: LocalLimitOrderDao,
    private val orderFilterDao: OrderFilterDao,
    private val tokenDao: TokenDao,
    private val tokenExtDao: TokenExtDao,
    private val marketDao: MarketDao,
    private val selectedMarketDao: SelectedMarketDao,
    private val pendingBalancesDao: PendingBalancesDao,
    private val limitOrderApi: LimitOrderApi,
    private val tokenApi: TokenApi,
    private val tokenClient: TokenClient,
    private val orderMapper: OrderMapper,
    private val feeMapper: FeeMapper
) : LimitOrderRepository {
    override fun eligibleAddress(param: CheckEligibleAddressUseCase.Param): Single<EligibleAddress> {
        return limitOrderApi.eligibleAddress(param.wallet.address).map {
            orderMapper.transform(it)
        }
    }

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
                it,
                param.localLimitOrder.sideTrade

            ).map {
                orderMapper.transform(it)
            }.doAfterSuccess {
                if (it.success) {
                    limitOrderDao.insertOrder(it.order)
                }
            }
        }
    }

    override fun getLimitOrderFee(param: GetLimitOrderFeeUseCase.Param): Flowable<Fee> {
        return limitOrderApi.getFee(
            param.sourceToken.tokenAddress,
            param.destToken.tokenAddress,
            param.sourceAmount,
            param.destAmount,
            param.userAddress
        ).map {
            feeMapper.transform(it)
        }
            .repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun getCurrentLimitOrders(param: GetLocalLimitOrderDataUseCase.Param): Flowable<LocalLimitOrder> {
        return Flowable.fromCallable {
            val wallet = param.wallet
            val type = param.type
            val defaultLimitOrder = when (val limitOrder =
                localLimitOrderDao.findLocalLimitOrderByAddress(wallet.address, type)) {
                null -> {
                    val ethStarToken = getEthStarToken()
                    val kncToken = tokenDao.getTokenBySymbol(Token.KNC)
                    //                    val order = LocalLimitOrder(
//                        wallet.address,
//                        defaultSourceToken ?: Token(),
//                        defaultDestToken ?: Token(),
//                        type = type
//                    )
//
//                    localLimitOrderDao.insertOrder(order)
//                    order

                    val srcToken = if (type == LocalLimitOrder.TYPE_SELL) {
                        kncToken
                    } else {
                        ethStarToken
                    }

                    val dstToken = if (type == LocalLimitOrder.TYPE_SELL) {
                        ethStarToken
                    } else {
                        kncToken
                    }

                    LocalLimitOrder(
                        wallet.address,
                        tokenSource = srcToken ?: Token(),
                        tokenDest = dstToken ?: Token(),
                        type = type
                    )
                }
                else -> limitOrder
            }

            val source = getRecentBalance(defaultLimitOrder.tokenSource, wallet)
            val dest = getRecentBalance(defaultLimitOrder.tokenDest, wallet)

            val order = when {
                source.isETHWETH -> {
                    val ethToken =
                        tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
                    val wethToken =
                        tokenDao.getTokenBySymbol(Token.WETH_SYMBOL) ?: Token()

                    defaultLimitOrder.copy(
                        ethToken = ethToken,
                        wethToken = wethToken,
                        tokenSource = source,
                        tokenDest = dest
                    )
                }
                else -> defaultLimitOrder.copy(
                    tokenSource = source,
                    tokenDest = dest
                )
            }
            localLimitOrderDao.insertOrder(order)
            order
        }.flatMap {
            localLimitOrderDao.findLocalLimitOrderByAddressFlowable(
                param.wallet.address,
                param.type
            )
                .defaultIfEmpty(it)
        }
    }

    private fun getTokenBalance(
        symbol: String,
        wallet: Wallet
    ): BigDecimal {
        val token = tokenDao.getTokenBySymbol(symbol)
        return if (token?.selectedWalletAddress != wallet.address) {
            token?.updateSelectedWallet(wallet)
        } else {
            token
        }?.currentBalance ?: BigDecimal.ZERO
    }

    private fun getRecentBalance(token: Token, wallet: Wallet): Token {
        return when {
            token.isETHWETH -> {
                val ethBalance = getTokenBalance(Token.ETH_SYMBOL, wallet)
                val wethBalance = getTokenBalance(Token.WETH_SYMBOL, wallet)
                token.updateBalance(
                    ethBalance.plus(wethBalance)
                )
            }
            else -> {

                val updatedBalanceToken = tokenDao.getTokenByAddress(token.tokenAddress) ?: token
                when {
                    updatedBalanceToken.selectedWalletAddress != wallet.address -> updatedBalanceToken.updateSelectedWallet(
                        wallet
                    )
                    else -> updatedBalanceToken
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

    private fun getEthStarToken(): Token? {
        val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
        val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)

        val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO
        val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

        return wethToken?.updateBalance(ethBalance.plus(wethBalance))?.copy(
            tokenSymbol = Token.ETH_SYMBOL_STAR
        )
    }

    override fun saveSelectedMarket(param: SaveSelectedMarketUseCase.Param): Completable {
        return Completable.fromCallable {
            val walletAddress = param.wallet.walletAddress
            var currentMarket =
                selectedMarketDao.getSelectedMarketByWalletAddress(walletAddress)
            if (currentMarket != null) {
                currentMarket = currentMarket.copy(pair = param.marketItem.pair)
                selectedMarketDao.updateSelectedMarket(currentMarket)
            } else {
                currentMarket = SelectedMarketItem(
                    walletAddress,
                    param.marketItem.pair
                )
                selectedMarketDao.insertSelectedMarket(
                    currentMarket
                )
            }

            val ethStarToken = getEthStarToken()

            val quoteToken =
                if (currentMarket.quote.equals(Token.ETH_SYMBOL_STAR, true)) {
                    ethStarToken
                } else {
                    tokenDao.getKyberListedTokenBySymbol(currentMarket.quote)
                } ?: Token()

            val srcToken = getRecentBalance(quoteToken, param.wallet)

            val baseToken =
                if (currentMarket.base.equals(Token.ETH_SYMBOL_STAR, true)) {
                    ethStarToken
                } else {
                    tokenDao.getKyberListedTokenBySymbol(currentMarket.base)
                } ?: Token()

            val dstToken = getRecentBalance(baseToken, param.wallet)

            val buyOrder = LocalLimitOrder(
                walletAddress,
                tokenSource = srcToken,
                tokenDest = dstToken,
                type = LocalLimitOrder.TYPE_BUY
            )

            val sellOrder = LocalLimitOrder(
                walletAddress,
                tokenSource = dstToken,
                tokenDest = srcToken,
                type = LocalLimitOrder.TYPE_SELL
            )

            localLimitOrderDao.insertOrder(buyOrder)
            localLimitOrderDao.insertOrder(sellOrder)

        }
    }

    override fun getSelectedMarket(param: GetSelectedMarketUseCase.Param): Flowable<SelectedMarketItem> {
        return Flowable.fromCallable {
            var market = selectedMarketDao.getSelectedMarketByWalletAddress(param.wallet.address)
            if (market == null) {
                market = SelectedMarketItem(param.wallet.address, MarketItem.DEFAULT_PAIR)
                selectedMarketDao.insertSelectedMarket(market)
            }
            market
        }.flatMap {
            selectedMarketDao.getSelectedMarketByWalletAddressFlowable(it.walletAddress)
        }
    }

    override fun getMarket(param: GetMarketUseCase.Param): Flowable<MarketItem> {
        return marketDao.getMarketByPairFlowable(param.pair)
    }

    override fun getFavoritePairs(): Single<List<FavoritePair>> {
        return limitOrderApi.getFavoritePairs().map {
            if (it.success) {
                it.favoritePairs
            } else {
                throw RuntimeException("can't get the favorite pairs")
            }
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
                mutableListOf<OrderEntity>(),
                { builder, response ->
                    if (response.pagingInfo.pageIndex == 1) {
                        builder.clear()
                    }
                    builder.addAll(response.orders)
                    builder
                })
            .map {
                orderMapper.transform(it)
            }.toFlowable()
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

            Flowable.fromCallable {
                pendingBalancesDao.pendingBalancesByWalletAddress(param.wallet.address) != null
            }.flatMap {
                if (it) {
                    pendingBalancesDao.pendingBalancesByWalletAddressFlowable(param.wallet.address)
                } else {
                    Flowable.fromCallable {
                        PendingBalances()
                    }
                }
            },
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

    override fun pollingMarket(): Flowable<List<MarketItem>> {
        return tokenApi.getPairMarket().map {
            if (it.error) {
                throw RuntimeException()
            } else {
                it.data.map { entity ->
                    MarketItem(entity)
                }
            }
        }
            .doAfterSuccess { markets ->
                val spLimitOrder = tokenDao.limitOrderTokens.filter { it.isListed }.map {
                    it.symbol.toLowerCase(Locale.getDefault())
                }

                val favMarket = marketDao.favMarkets.map {
                    it.pair to it.isFav
                }.toMap()

                val combinedMarkets = markets.filter { item ->
                    spLimitOrder.contains(
                        item.pair.split("_").last().toLowerCase(Locale.getDefault())
                    )
                }.groupBy { it.combinedPair }
                    .map { maps ->
                        var volume: BigDecimal = BigDecimal.ZERO
                        var change: String? = null
                        var pair = ""
                        maps.value.forEach {
                            volume += it.volume.toBigDecimalOrDefaultZero()
                            if (it.pair.contains(Token.WETH_SYMBOL)) {
                                change = it.change
                            }
                            pair = it.combinedPair
                        }

                        val item = maps.value.first()
                        item.copy(
                            pair = pair,
                            change = change ?: item.change,
                            volume = volume.toDisplayNumber(),
                            isFav = favMarket[pair] ?: false
                        )
                    }
                marketDao.updateLatestMarketItem(combinedMarkets)
            }
            .toFlowable()
            .repeatWhen {
                it.delay(60, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun getMarkets(forceRefresh: Boolean): Flowable<List<MarketItem>> {
        return if (forceRefresh) {
            tokenApi.getPairMarket()
                .map {
                    if (it.error) {
                        throw RuntimeException()
                    } else {
                        it.data.map { entity ->
                            MarketItem(entity)
                        }
                    }
                }
                .doAfterSuccess { markets ->

                    val spLimitOrder = tokenDao.limitOrderTokens.filter { it.isListed }.map {
                        it.symbol.toLowerCase(Locale.getDefault())
                    }

                    val favMarket = marketDao.favMarkets.map {
                        it.pair to it.isFav
                    }.toMap()

                    val combinedMarkets = markets.filter { item ->
                        spLimitOrder.contains(
                            item.pair.split("_").last().toLowerCase(Locale.getDefault())
                        )
                    }.groupBy { it.combinedPair }
                        .map { maps ->
                            var volume: BigDecimal = BigDecimal.ZERO
                            var change = ""
                            var pair = ""
                            maps.value.forEach {
                                volume += it.volume.toBigDecimalOrDefaultZero()
                                if (it.pair.contains(Token.WETH_SYMBOL)) {
                                    change = it.change
                                }
                                pair = it.combinedPair
                            }

                            val item = maps.value.first()
                            item.copy(
                                pair = pair,
                                change = change,
                                volume = volume.toDisplayNumber(),
                                isFav = favMarket[pair] ?: false
                            )
                        }
                    marketDao.updateLatestMarketItem(combinedMarkets)
                }
                .toFlowable()
        } else {
            marketDao.all
        }
    }

    override fun saveMarketIem(param: SaveMarketItemUseCase.Param): Single<ResponseStatus> {
        if (param.isLogin) {
            val base = param.marketItem.baseSymbol
            val quote = param.marketItem.quoteSymbol
            val body =
                RequestBody.create(
                    MediaType.parse("text/plain"),
                    Gson().toJson(
                        FavoritePairStatus(
                            if (base.equals(
                                    Token.ETH_SYMBOL_STAR,
                                    true
                                )
                            ) Token.WETH_SYMBOL else base,
                            if (quote.equals(
                                    Token.ETH_SYMBOL_STAR,
                                    true
                                )
                            ) Token.WETH_SYMBOL else quote,
                            param.marketItem.isFav
                        )
                    )
                )
            return limitOrderApi.favPair(body).map {
                ResponseStatus(it)
            }
        } else {
            return Single.fromCallable {
                val marketByPair = marketDao.getMarketByPair(param.marketItem.pair)
                marketByPair?.let {
                    marketDao.updateMarket(it.copy(isFav = param.marketItem.isFav))
                }
                ResponseStatus(success = true)
            }
        }
    }

    override fun getStableQuoteTokens(): Single<List<String>> {
        return Single.fromCallable {
            tokenExtDao.allTokens
                .filter {
                    it.quotePriority == 3
                }.map {
                    it.tokenSymbol
                }
        }
    }
}