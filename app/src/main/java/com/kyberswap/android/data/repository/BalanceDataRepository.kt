package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.CurrencyApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.balance.UpdateBalanceUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceDataRepository @Inject constructor(
    private val context: Context,
    private val api: TokenApi,
    private val currencyApi: CurrencyApi,
    private val tokenMapper: TokenMapper,
    private val tokenClient: TokenClient,
    private val tokenDao: TokenDao,
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
                val tokenSource = tokenDao.getTokenBySymbol(it.sourceSymbol) ?: Token()
                val tokenDest = tokenDao.getTokenBySymbol(it.destSymbol) ?: Token()
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
                    tokenDao.getTokenBySymbol(send.tokenSource.tokenSymbol)
                        ?: Token()

                val ethToken = tokenDao.getTokenBySymbol(Token.ETH) ?: Token()
                val updatedSend = it.copy(tokenSource = tokenSource, ethToken = ethToken)
                if (updatedSend != it) {
                    sendDao.updateSend(updatedSend)
                }
            }

            val limitOrder = localLimitOrderDao.findLocalLimitOrderByAddress(wallet.address)
            limitOrder?.let {

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
                tokenDao.getTokenBySymbol(token.tokenSymbol) ?: token
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

    override fun getTokenBalances(param: GetTokensBalanceUseCase.Param): Completable {
        return Completable.fromCallable {
            val updatedTokens = tokenClient.updateBalances(
                param.wallet.address,
                context.getString(R.string.wrapper_contract_address),
                param.tokens
            )
            tokenDao.updateTokens(updatedTokens)

        }
    }

    override fun getChange24h(): Flowable<List<Token>> {
        return tokenDao.all
    }

    override fun getBalance(param: PrepareBalanceUseCase.Param): Single<List<Token>> {
        return if (tokenDao.all.blockingFirst().isEmpty() || param.forceUpdate) {
            fetchChange24h()
                .flatMap { remoteTokenList ->
                    currencyApi.internalCurrencies()
                        .map { currencies -> currencies.data }
                        .toFlowable()
                        .flatMapIterable { tokenCurrency -> tokenCurrency }
                        .map { internalCurrency ->
                            val tokenBySymbol = remoteTokenList.find {
                                it.tokenSymbol == internalCurrency.symbol
                            }
                            tokenBySymbol?.with(internalCurrency) ?: Token(internalCurrency)
                        }
                        .toList()
                        .map { remoteTokens ->
                            val localTokenList = tokenDao.all.first(listOf()).blockingGet()
                            remoteTokens.map { remoteToken ->
                                val localToken = localTokenList.find {
                                    it.tokenAddress == remoteToken.tokenAddress
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

                .doAfterSuccess {
                    tokenDao.insertTokens(it)
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

        return Singles.zip(
            singleEthSource,
            singleUsdSource,
            singleChange24hSource
        ) { eth, usd, change24h ->
            updateRate(eth, usd, change24h)
        }
            .map { it.values.toList() }
    }


    private fun updateBalance(
        remoteTokens: List<Token>,
        wallets: List<Wallet>
    ): List<Token> {

        val localTokens = tokenDao.allTokens.map {
            it.updateSelectedWallet(wallets).copy(isOther = true)
        }

        val listedTokens = remoteTokens.map { remoteToken ->
            val localToken = localTokens.find {
                it.tokenAddress == remoteToken.tokenAddress
            }

            val updatedRateToken = localToken?.copy(
                rateUsdNow = remoteToken.rateUsdNow,
                rateEthNow = remoteToken.rateEthNow,
                changeEth24h = remoteToken.changeEth24h,
                changeUsd24h = remoteToken.changeUsd24h,
                tokenName = remoteToken.tokenName,
                tokenSymbol = remoteToken.tokenSymbol,
                isOther = false
            ) ?: remoteToken

            updatedRateToken
//            tokenClient.updateBalance(updatedRateToken)
        }

        val listTokenSymbols = listedTokens.map { it.tokenSymbol }

        val otherTokens = localTokens.filterNot { listTokenSymbols.contains(it.tokenSymbol) }
//            .map {
//            tokenClient.updateBalance(it)
//        }

        val currentWallets = walletDao.all
        val localSelected = currentWallets.find { it.isSelected }
        val selectedWallet = wallets.find { it.isSelected }

        return if (selectedWallet?.address == localSelected?.address) {
            val currentFavs = tokenDao.allTokens.map {
                it.tokenSymbol to it.fav
            }.toMap()
            tokenDao.updateTokens(listedTokens.map {
                it.copy(fav = currentFavs[it.tokenSymbol] ?: false)
            })
            tokenDao.updateTokens(otherTokens.map {
                it.copy(fav = currentFavs[it.tokenSymbol] ?: false)
            })
            listedTokens
        } else {
            updateBalance(remoteTokens, currentWallets)
        }
    }

    override fun getChange24hPolling(param: GetBalancePollingUseCase.Param): Flowable<List<Token>> {
        return fetchChange24h()
            .map { remoteTokens ->
                updateBalance(remoteTokens, param.wallets)
            }
            .repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }


    private fun updateRate(
        eth: Map<String, BigDecimal>,
        usd: Map<String, BigDecimal>,
        change24h: Map<String, Token>
    ): Map<String, Token> {

        return change24h.map { token ->
            token.key to token.value.copy(
                rateEthNow = eth[token.value.tokenSymbol] ?: token.value.rateEthNow,
                rateUsdNow = usd[token.value.tokenSymbol] ?: token.value.rateUsdNow
            )

        }.toMap()
    }

    companion object {
        const val ETH = "ETH"
        const val USD = "USD"
    }
}