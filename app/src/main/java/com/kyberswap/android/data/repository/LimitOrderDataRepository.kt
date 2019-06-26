package com.kyberswap.android.data.repository

import android.content.Context
import android.util.Base64
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.LimitOrderApi
import com.kyberswap.android.data.db.LimitOrderDao
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.OrderFilterDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.mapper.FeeMapper
import com.kyberswap.android.data.mapper.OrderMapper
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.limitorder.*
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.displayWalletAddress
import com.kyberswap.android.util.ext.hexWithPrefix
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.WalletManager
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import javax.inject.Inject


class LimitOrderDataRepository @Inject constructor(
    private val context: Context,
    private val limitOrderDao: LimitOrderDao,
    private val localLimitOrderDao: LocalLimitOrderDao,
    private val orderFilterDao: OrderFilterDao,
    private val tokenDao: TokenDao,
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
.doAfterSuccess { status ->
            if (status.cancelled) {
                val order = limitOrderDao.findOrderById(param.order.id)
                order?.let {
                    limitOrderDao.updateOrder(it.copy(status = Order.Status.CANCELLED.value))
        
    

    }

    override fun saveOrderFilter(param: SaveLimitOrderFilterUseCase.Param): Completable {
        return Completable.fromCallable {
            val addresses = param.orderFilter.listAddress.filter {
                it.isSelected
    .map {
                it.name
    

            val pairs = param.orderFilter.listOrders.filter {
                it.isSelected
    .map {
                val pair = it.name.split(TOKEN_PAIR_SEPARATOR)
                pair.first() to pair.last()
    

            val status = param.orderFilter.listStatus.filter {
                it.isSelected
    .map {
                when (it.name) {
                    context.getString(R.string.order_status_open) -> Order.Status.OPEN
                    context.getString(R.string.order_status_invalidated) -> Order.Status.INVALIDATED
                    context.getString(R.string.order_status_in_progress) -> Order.Status.IN_PROGRESS
                    context.getString(R.string.order_status_cancelled) -> Order.Status.CANCELLED
                    context.getString(R.string.order_status_filled) -> Order.Status.FILLED
                    else -> Order.Status.UNKNOWN
        .value
    

            val filter = param.orderFilter.apply {
                this.addresses = addresses
                this.status = status
                this.pairs = pairs
    
            orderFilterDao.updateOrderFilter(filter)

    }

    override fun getOrderFilter(param: GetLimitOrderFilterUseCase.Param): Flowable<OrderFilter> {
        val filter = orderFilterDao.findOrderFilterByAddress(param.walletAddress)
        val orderFilter = filter ?: OrderFilter(
            status = listOf(Order.Status.OPEN.value, Order.Status.IN_PROGRESS.value)
        )

        val orders = limitOrderDao.findAllOrdersByAddress(param.walletAddress)
        val pairs = mutableSetOf<Pair<String, String>>()
        val address = mutableSetOf<String>()

        orders.forEach {
            pairs.add(Pair(it.src, it.dst))
            address.add(it.userAddr)


        val listOrders = pairs.map {
            FilterItem(
                orderFilter.pairs.indexOf(it) >= 0,
                StringBuilder().append(it.first).append(TOKEN_PAIR_SEPARATOR).append(it.second)
                    .toString()
            )


        val listAddress = address.map {
            FilterItem(
                orderFilter.addresses.indexOf(it.displayWalletAddress()) >= 0,
                it.displayWalletAddress()
            )


        val listStatus = listOf(
            Order.Status.OPEN,
            Order.Status.IN_PROGRESS,
            Order.Status.FILLED,
            Order.Status.CANCELLED,
            Order.Status.INVALIDATED
        ).map {
            val display = when (it) {
                Order.Status.OPEN -> context.getString(R.string.order_status_open)
                Order.Status.INVALIDATED -> context.getString(R.string.order_status_invalidated)
                Order.Status.IN_PROGRESS -> context.getString(R.string.order_status_in_progress)
                Order.Status.CANCELLED -> context.getString(R.string.order_status_cancelled)
                Order.Status.FILLED -> context.getString(R.string.order_status_filled)
                Order.Status.UNKNOWN -> it.value
    

            FilterItem(orderFilter.status.indexOf(it.value) >= 0, display)


        val update = orderFilter.copy(
            walletAddress = param.walletAddress,
            listOrders = listOrders,
            listAddress = listAddress,
            listStatus = listStatus
        )
        if (orderFilter != update) {
            orderFilterDao.insertOrderFilter(update)

        return orderFilterDao.findOrderFilterByAddressFlowable(param.walletAddress).defaultIfEmpty(
            update
        ).map {
            it.copy(
                walletAddress = param.walletAddress,
                listOrders = listOrders,
                listAddress = listAddress,
                listStatus = listStatus
            )

    }

    override fun getNonce(param: GetNonceUseCase.Param): Single<String> {
        return limitOrderApi.getNonce(
            param.wallet.address,
            param.limitOrder.tokenSource.tokenAddress,
            param.limitOrder.tokenDest.tokenAddress
        ).map {
            it.nonce

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
.flatMap { it ->
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
    .doAfterSuccess {
                if (it.success) {
                    limitOrderDao.insertOrder(it.order)
        
    



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

    override fun getCurrentLimitOrders(param: GetLocalLimitOrderDataUseCase.Param): Flowable<LocalLimitOrder> {
        val limitOrder = localLimitOrderDao.findLocalLimitOrderByAddress(param.walletAddress)
        val defaultLimitOrder = if (limitOrder == null) {
            val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)
            val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)

            val ethBalance = ethToken?.currentBalance ?: BigDecimal.ZERO
            val wethBalance = wethToken?.currentBalance ?: BigDecimal.ZERO

            val defaultSourceToken = wethToken?.updateBalance(ethBalance.plus(wethBalance))?.copy(
                tokenSymbol = Token.ETH_SYMBOL_STAR
            )

            val defaultDestToken = tokenDao.getTokenBySymbol(Token.KNC)

            val order = LocalLimitOrder(
                param.walletAddress,
                defaultSourceToken ?: Token(),
                defaultDestToken ?: Token()
            )

            localLimitOrderDao.insertOrder(order)
            order
 else {
            limitOrder


        val source = updateBalance(defaultLimitOrder.tokenSource)
        val dest = updateBalance(defaultLimitOrder.tokenDest)

        val order = if (source.isETHWETH) {
            val ethToken =
                tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
            val wethToken =
                tokenDao.getTokenBySymbol(Token.WETH_SYMBOL) ?: Token()

            defaultLimitOrder.copy(
                ethToken = ethToken,
                wethToken = wethToken
            )
 else {
            defaultLimitOrder


        val orderWithToken = order.copy(
            tokenSource = source,
            tokenDest = dest
        )

        if (orderWithToken != defaultLimitOrder) {
            localLimitOrderDao.insertOrder(orderWithToken)


        return localLimitOrderDao.findLocalLimitOrderByAddressFlowable(param.walletAddress)
            .defaultIfEmpty(orderWithToken)
    }

    private fun updateBalance(token: Token): Token {
        return if (token.isETHWETH) {
            val ethBalance =
                tokenDao.getTokenBySymbol(Token.ETH_SYMBOL)?.currentBalance ?: BigDecimal.ZERO
            val wethBalance =
                tokenDao.getTokenBySymbol(Token.WETH_SYMBOL)?.currentBalance ?: BigDecimal.ZERO
            token.updateBalance(
                ethBalance.plus(
                    wethBalance
                )
            )
 else {
            token

    }


    override fun saveLimitOrder(param: SaveLimitOrderUseCase.Param): Completable {
        return Completable.fromCallable {
            localLimitOrderDao.insertOrder(param.order)

    }

    override fun saveLimitOrder(param: SaveLimitOrderTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val currentLimitOrderForWalletAddress =
                localLimitOrderDao.findLocalLimitOrderByAddress(param.walletAddress)
            val order = if (param.isSourceToken) {
                currentLimitOrderForWalletAddress?.copy(tokenSource = param.token)
     else {
                currentLimitOrderForWalletAddress?.copy(tokenDest = param.token)
    
            order?.let { localLimitOrderDao.updateOrder(it) }

    }

    override fun getLimitOrders(param: GetLimitOrdersUseCase.Param): Flowable<List<Order>> {
        return Flowable.mergeDelayError(limitOrderDao.findOrdersByAddressFlowable(param.walletAddress),
            limitOrderApi.getOrders(param.walletAddress)
                .map {
                    it.orders
        
                .map {
                    orderMapper.transform(it)
        
                .doAfterSuccess {
                    limitOrderDao.insertOrders(it)
        .toFlowable()
        )
    }

    override fun getRelatedLimitOrders(param: GetRelatedLimitOrdersUseCase.Param): Flowable<List<Order>> {
        return Flowable.mergeDelayError(
            limitOrderDao.findOrdersByAddressFlowable(param.walletAddress).map {
                it.filter { order ->
                    order.src ==
                        param.tokenSource.submitOrderTokenSymbol && order.dst == param.tokenDest.submitOrderTokenSymbol &&
                        order.isPending
        
    ,
            limitOrderApi.getRelatedOrders(
                param.walletAddress,
                param.tokenSource.tokenAddress,
                param.tokenDest.tokenAddress,
                param.status
            )
                .map {
                    it.orders
        
                .map {
                    orderMapper.transform(it)
        
                .map {
                    it.filter { it.isPending }
        
                .doAfterSuccess {
                    limitOrderDao.insertOrders(it)
        .toFlowable()
        )

    }

    companion object {
        const val TOKEN_PAIR_SEPARATOR = "  ➞  "
        const val ADDRESS_SEPARATOR = "..."
    }
}