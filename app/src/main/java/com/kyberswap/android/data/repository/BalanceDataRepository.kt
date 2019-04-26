package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.domain.model.token.Token
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.util.TokenClient
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import java.math.BigDecimal
import javax.inject.Inject

class BalanceDataRepository @Inject constructor(
    val api: TokenApi,
    private val tokenMapper: TokenMapper,
    private val tokenClient: TokenClient
) :
    BalanceRepository {
    override fun getBalance(owner: String, token: Token): Single<Token> {
        return Single.fromCallable {
            tokenClient.getBalance(owner, token)

    }

    override fun getBalance(owner: String, tokenList: List<Token>): Single<List<Token>> {
        return Single.fromCallable {
            tokenList.forEach {
                tokenClient.getBalance(owner, it)
    
            tokenList

    }

    override fun getChange24h(owner: String): Flowable<Token> {
        val singleEthSource = api.tokenPrice(ETH).map {
            it.data.map { data -> data.symbol to data.price }.toMap()


        val singleUsdSource = api.tokenPrice(USD).map {
            it.data.map { data -> data.symbol to data.price }.toMap()


        val singleChange24hSource = api.getChange24h().map { response ->
            response.entries.associate { it.key to tokenMapper.transform(it.value) }


        return Singles.zip(
            singleEthSource,
            singleUsdSource,
            singleChange24hSource
        ) { eth, usd, change24h ->
            updateRate24h(eth, usd, change24h)
.map { it.values }
            .toFlowable()
            .flatMapIterable { token -> token }
            .flatMap {
                getBalance(owner, it).toFlowable()
    
    }

    private fun updateRate24h(
        eth: Map<String, BigDecimal>,
        usd: Map<String, BigDecimal>,
        change24h: Map<String, Token>
    ): Map<String, Token> {
        change24h.forEach { (key, value) ->
            value.rateEthNow = eth[value.tokenName] ?: value.rateEthNow
            value.rateUsdNow = usd[value.tokenName] ?: value.rateUsdNow
            key to value

        return change24h
    }

    companion object {
        const val ETH = "ETH"
        const val USD = "USD"
    }
}