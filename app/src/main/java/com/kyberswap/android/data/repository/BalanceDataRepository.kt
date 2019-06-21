package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.CurrencyApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import com.kyberswap.android.util.TokenClient
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.Singles
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BalanceDataRepository @Inject constructor(
    private val api: TokenApi,
    private val currencyApi: CurrencyApi,
    private val tokenMapper: TokenMapper,
    private val tokenClient: TokenClient,
    private val tokenDao: TokenDao,
    private val walletDao: WalletDao
) :
    BalanceRepository {

//    override fun getBalance(owner: String, token: Token): Single<Token> {
//        return Single.fromCallable {
//            tokenClient.getBalance(owner, token)
//        }
//    }
//
//    override fun getBalance(owner: String, tokenList: List<Token>): Single<List<Token>> {
//        return Single.fromCallable {
//            tokenList.forEach {
//                tokenClient.getBalance(owner, it)
//            }
//            tokenList
//        }
//    }

    override fun getChange24h(): Flowable<List<Token>> {
        return tokenDao.all
    }

    override fun getBalance(param: PrepareBalanceUseCase.Param): Single<List<Token>> {
        return if (tokenDao.all.blockingFirst().isEmpty() || param.forceUpdate) {
            fetchChange24h()
                .flatMap { tokenList ->
                    //                    tokenDao.insertTokens(it)
                    currencyApi.internalCurrencies()
                        .map { currencies -> currencies.data }
                        .toFlowable()
                        .flatMapIterable { tokenCurrency -> tokenCurrency }
                        .map { internalCurrency ->
                            val tokenBySymbol = tokenList.find {
                                it.tokenSymbol == internalCurrency.symbol
                            }
//                                tokenDao.getTokenBySymbol(internalCurrency.symbol)
                            tokenBySymbol?.with(internalCurrency) ?: Token(internalCurrency)
                        }
                        .toList()
                        .map {
                            val currentList = tokenDao.all.first(listOf()).blockingGet()
                            it.map { token ->
                                val currentToken = currentList.find {
                                    it.tokenSymbol == token.tokenSymbol
                                }

                                if (currentToken != null) {
                                    token.copy(wallets = currentToken.wallets)
                                } else {
                                    token
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

    override fun getChange24hPolling(owner: String): Flowable<List<Token>> {
        return fetchChange24h()
            .map { tokens ->
                val allTokens = tokenDao.allTokens
                tokens.map { token ->
                    val currentToken = allTokens.find {
                        it.tokenAddress == token.tokenAddress
                    }

                    val updatedWithBalance = if (currentToken == null) {
                        token
                    } else {
                        token.copy(wallets = currentToken.wallets)
                    }
                    tokenClient.getBalance(updatedWithBalance)
                }
            }
            .doAfterSuccess {
                tokenDao.updateTokens(it)
            }
            .repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    private fun <T> zipWithFlatMap(): FlowableTransformer<T, Long> {
        return FlowableTransformer { flowable ->
            flowable.zipWith(
                Flowable.range(COUNTER_START, ATTEMPTS),
                BiFunction<T, Int, Int> { _: T, u: Int -> u })
                .flatMap { t -> Flowable.timer(t * 5L, TimeUnit.SECONDS) }
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
        private const val COUNTER_START = 1
        private const val ATTEMPTS = 5
    }
}