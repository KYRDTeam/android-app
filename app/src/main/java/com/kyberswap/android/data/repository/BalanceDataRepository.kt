package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.KyberSwapApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TokenExtDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.TokenExt
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.balance.UpdateBalanceUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetOtherBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetOtherTokenBalancesUseCase
import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.presentation.common.MIN_SUPPORT_AMOUNT
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import timber.log.Timber
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceDataRepository @Inject constructor(
    private val context: Context,
    private val api: TokenApi,
    private val kyberSwapApi: KyberSwapApi,
    private val tokenMapper: TokenMapper,
    private val tokenClient: TokenClient,
    private val tokenDao: TokenDao,
    private val tokenExtDao: TokenExtDao,
    private val walletDao: WalletDao,
    private val swapDao: SwapDao,
    private val sendDao: SendDao,
    private val localLimitOrderDao: LocalLimitOrderDao
) :
    BalanceRepository {

    override fun updateBalance(param: UpdateBalanceUseCase.Param): Completable {
        return Completable.fromCallable {
            val wallet = param.wallet
            val localSwap = swapDao.findSwapByAddress(wallet.address)
            localSwap?.let {
                val tokenSource = tokenDao.getTokenByAddress(it.sourceAddress) ?: Token()
                val tokenDest = tokenDao.getTokenByAddress(it.destAddress) ?: Token()
                val tokenEth = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()

                val updatedToken =
                    it.copy(tokenSource = tokenSource, tokenDest = tokenDest, ethToken = tokenEth)
                if (updatedToken != it) {
                    swapDao.updateSwap(updatedToken)
                }

            }

            val send = sendDao.findSendByAddress(wallet.address)
            send?.let {
                val tokenSource =
                    tokenDao.getTokenByAddress(send.tokenSource.tokenAddress)
                        ?: Token()

                val ethToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
                val updatedSend = it.copy(tokenSource = tokenSource, ethToken = ethToken)
                if (updatedSend != it) {
                    sendDao.updateSend(updatedSend)
                }
            }

            val limitOrders = localLimitOrderDao.findAllLimitOrderByAddress(wallet.address)
            limitOrders.forEach {
                val source = updateBalance(it.tokenSource)
                val dest = updateBalance(it.tokenDest)

                val order = when {
                    source.isETHWETH -> {
                        val ethToken =
                            tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
                        val wethToken =
                            tokenDao.getTokenBySymbol(Token.WETH_SYMBOL) ?: Token()

                        it.copy(
                            ethToken = ethToken,
                            wethToken = wethToken
                        )
                    }
                    else -> it
                }

                val orderWithToken = order.copy(
                    tokenSource = source,
                    tokenDest = dest
                )

                if (orderWithToken != it) {
                    localLimitOrderDao.insertOrder(orderWithToken)
                }

            }
        }
    }

    private fun updateBalance(token: Token): Token {
        return when {
            token.isETHWETH -> {

                val ethToken = tokenDao.getTokenBySymbol(Token.ETH_SYMBOL) ?: Token()
                val wethToken = tokenDao.getTokenBySymbol(Token.WETH_SYMBOL) ?: Token()
                val ethBalance = ethToken.currentBalance

                val wethBalance = wethToken.currentBalance

                token.updateBalance(
                    ethBalance.plus(wethBalance)
                )
            }
            else -> {
                tokenDao.getTokenByAddress(token.tokenAddress) ?: token
            }
        }
    }

    override fun getTokenBalance(token: Token): Completable {
        return Completable.fromCallable {
            val updatedToken = tokenClient.updateBalance(token)
            if (token.currentBalance != updatedToken.currentBalance) {
                tokenDao.updateToken(updatedToken)
            }

        }
    }

    override fun getOtherTokenBalances(param: GetOtherTokenBalancesUseCase.Param): Completable {
        return Completable.fromCallable {
            try {
                val otherList = param.otherTokens.map { token ->
                    val updatedToken = tokenClient.updateBalance(token)
                    if (token.currentBalance != updatedToken.currentBalance) {
                        tokenDao.updateToken(updatedToken)
                        updatedToken
                    } else {
                        token
                    }
                }
                tokenDao.updateTokens(otherList)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Timber.e(ex.localizedMessage)
            }
        }
    }

    override fun getTokenBalances(param: GetTokensBalanceUseCase.Param): Completable {
        return Completable.fromCallable {
            val updatedTokens = tokenClient.updateBalances(
                context.getString(R.string.wrapper_contract_address),
                param.tokens
            )
            tokenDao.updateTokens(updatedTokens)
        }
    }

    override fun getLocalTokens(): Flowable<List<Token>> {
        return tokenDao.all.map {
            it.distinctBy { token -> token.tokenAddress.toLowerCase(Locale.getDefault()) }
                .sortedBy { it.tokenSymbol }
        }
    }

    override fun getBalance(): Flowable<List<Token>> {
        return getLocalTokens().first(listOf()).mergeWith(
            fetchChange24h()
                .flatMap { remoteTokenList ->
                    kyberSwapApi.internalCurrencies()
                        .map { currencies -> currencies.data }
                        .toFlowable()
                        .flatMapIterable { tokenCurrency -> tokenCurrency }
                        .map { internalCurrency ->
                            val tokenBySymbol = remoteTokenList.find {
                                it.tokenSymbol.equals(internalCurrency.symbol, true)
                            }

                            tokenBySymbol?.with(internalCurrency) ?: Token(internalCurrency)
                        }
                        .toList()
                        .doAfterSuccess { tokens ->
                            tokenExtDao.batchInsertTokenExtras(tokens.map {
                                TokenExt(it)
                            })
                        }
                        .map { remoteTokens ->
                            val localTokenList = tokenDao.all.first(listOf()).blockingGet()
                            val remoteList = remoteTokens.map { remoteToken ->
                                val localToken = localTokenList.find {
                                    it.tokenAddress.equals(remoteToken.tokenAddress, true)
                                }

                                if (localToken != null) {
                                    remoteToken.copy(
                                        wallets = localToken.wallets,
                                        fav = localToken.fav
                                    )
                                } else {
                                    remoteToken
                                }
                            }
                            remoteList.union(tokenDao.otherTokens).toList()
                                .sortedBy { it.tokenSymbol }
                        }
                }
                .doAfterSuccess { tokens ->
                    tokenDao.insertTokens(tokens)
                })
    }

    override fun preloadTokenInfo(): Single<List<Token>> {
        return if (tokenDao.all.blockingFirst().isEmpty()) {
            fetchChange24h()
                .doAfterSuccess {
                    tokenDao.insertTokens(it)
                }
                .flatMap { remoteTokenList ->
                    kyberSwapApi.internalCurrencies()
                        .map { currencies -> currencies.data }
                        .toFlowable()
                        .flatMapIterable { tokenCurrency -> tokenCurrency }
                        .map { internalCurrency ->
                            val tokenBySymbol = remoteTokenList.find {
                                it.tokenSymbol.equals(internalCurrency.symbol, true)
                            }

                            tokenBySymbol?.with(internalCurrency) ?: Token(internalCurrency)
                        }
                        .toList()
                        .doAfterSuccess { tokens ->
                            tokenExtDao.batchInsertTokenExtras(tokens.map {
                                TokenExt(it)
                            })
                        }
                        .map { remoteTokens ->
                            val localTokenList = tokenDao.all.first(listOf()).blockingGet()
                            remoteTokens.map { remoteToken ->
                                val localToken = localTokenList.find {
                                    it.tokenAddress.equals(remoteToken.tokenAddress, true)
                                }

                                if (localToken != null) {
                                    remoteToken.copy(
                                        wallets = localToken.wallets,
                                        fav = localToken.fav
                                    )
                                } else {
                                    remoteToken
                                }

                            }
                        }
                }

                .doAfterSuccess { tokens ->
                    tokenDao.insertTokens(tokens)
                }
        } else {
            tokenDao.all.first(listOf())
        }
    }

    private fun fetchChange24h(): Single<List<Token>> {
        val singleEthSource = api.tokenPrice(ETH).map {
            it.data.map { data -> data.symbol to data.price }.toMap()
        }

        val singleUsdSource = api.tokenPrice(USD).map {
            it.data.map { data -> data.symbol to data.price }.toMap()
        }

        val singleChange24hSource = api.getChange24h().map { response ->
            response.entries.associate { it.key to tokenMapper.transform(it.value) }
        }

        val singleListedToken = kyberSwapApi.internalCurrencies().map {
            it.data.map { data -> data.address.toLowerCase(Locale.getDefault()) to Token(data) }
                .toMap()
        }

        return Singles.zip(
            singleEthSource,
            singleUsdSource,
            singleChange24hSource,
            singleListedToken
        ) { eth, usd, change24h, listedToken ->
            updateTokenInfo(eth, usd, change24h, listedToken)
        }
            .map { it.values.toList() }
    }

    private fun updateTokenRate(
        remoteTokens: List<Token>,
        wallets: List<Wallet>
    ): List<Token> {

        val localTokens = tokenDao.allTokens.map {
            it.updateSelectedWallet(wallets).copy(isOther = true)
        }

        val listedTokens = remoteTokens.map { remoteToken ->
            val localToken = localTokens.find {
                it.tokenAddress.equals(remoteToken.tokenAddress, true)
            }

            val updatedRateToken = localToken?.copy(
                rateUsdNow = remoteToken.rateUsdNow,
                rateEthNow = remoteToken.rateEthNow,
                changeEth24h = remoteToken.changeEth24h,
                changeUsd24h = remoteToken.changeUsd24h,
                tokenName = remoteToken.tokenName,
                tokenSymbol = remoteToken.tokenSymbol,
                isOther = remoteToken.isOther,
                gasLimit = remoteToken.gasLimit,
                gasApprove = remoteToken.gasApprove,
                spLimitOrder = remoteToken.spLimitOrder,
                isQuote = remoteToken.isQuote
            ) ?: remoteToken
            updatedRateToken
        }

        val listTokenAddress = listedTokens.map { it.tokenAddress.toLowerCase(Locale.getDefault()) }

        val otherTokens = localTokens.filterNot {
            listTokenAddress.contains(
                it.tokenAddress.toLowerCase(
                    Locale.getDefault()
                )
            )
        }

        val currentWallets = walletDao.all
        val localSelected = currentWallets.find { it.isSelected }
        val selectedWallet = wallets.find { it.isSelected }

        return if (selectedWallet?.address.equals(localSelected?.address, true)) {
            val currentFavs = tokenDao.allTokens.map {
                it.tokenAddress.toLowerCase(Locale.getDefault()) to it.fav
            }.toMap()
            tokenDao.updateTokens(listedTokens.map {
                it.copy(
                    fav = currentFavs[it.tokenAddress.toLowerCase(Locale.getDefault())] ?: false
                )
            })
            tokenDao.updateTokens(otherTokens.map {
                it.copy(
                    fav = currentFavs[it.tokenAddress.toLowerCase(Locale.getDefault())] ?: false
                )
            })
            listedTokens
        } else {
            updateTokenRate(remoteTokens, currentWallets)
        }
    }

    override fun getChange24hPolling(param: GetBalancePollingUseCase.Param): Flowable<List<Token>> {
        return fetchChange24h()
            .map { remoteTokens ->
                updateTokenRate(remoteTokens, param.wallets)
            }
            .repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun getOthersBalancePolling(param: GetOtherBalancePollingUseCase.Param): Flowable<List<Token>> {
        var size = 0
        return Flowable.fromCallable {
            val otherList = tokenDao.otherTokens.toMutableList()
            val othersSize = otherList.size
            if (othersSize > 0 && othersSize > size) {
                size = othersSize
            } else {
                if (othersSize > 0) {
                    val removedOtherTokenList =
                        otherList.filter { it.allBalance < MIN_SUPPORT_AMOUNT }
                    if (removedOtherTokenList.isNotEmpty()) {
                        otherList.removeAll(removedOtherTokenList)
                    }
                }
            }
            otherList.toList()
        }.repeatWhen {
            it.delay(90, TimeUnit.SECONDS)
        }.retryWhen { throwable ->
            throwable.compose(zipWithFlatMap())
        }
    }

    private fun updateTokenInfo(
        eth: Map<String, BigDecimal>,
        usd: Map<String, BigDecimal>,
        change24h: Map<String, Token>,
        listedToken: Map<String, Token>
    ): Map<String, Token> {

        return change24h.map { token ->
            token.key to token.value.copy(
                rateEthNow = eth[token.value.tokenSymbol] ?: token.value.rateEthNow,
                rateUsdNow = usd[token.value.tokenSymbol] ?: token.value.rateUsdNow,
                isOther = listedToken[token.value.tokenAddress.toLowerCase(Locale.getDefault())] == null,
                gasLimit = listedToken[token.value.tokenAddress.toLowerCase(Locale.getDefault())]?.gasLimit
                    ?: "",
                gasApprove = listedToken[token.value.tokenAddress.toLowerCase(Locale.getDefault())]?.gasApprove
                    ?: BigDecimal.ZERO,
                spLimitOrder = listedToken[token.value.tokenAddress.toLowerCase(Locale.getDefault())]?.spLimitOrder
                    ?: false,
                isQuote = listedToken[token.value.tokenAddress.toLowerCase(Locale.getDefault())]?.isQuote
                    ?: false
            ).apply {
                quotePriority =
                    listedToken[token.value.tokenAddress.toLowerCase(Locale.getDefault())]?.quotePriority
                        ?: 0
            }

        }.toMap()
    }

    companion object {
        const val ETH = "ETH"
        const val USD = "USD"
    }
}