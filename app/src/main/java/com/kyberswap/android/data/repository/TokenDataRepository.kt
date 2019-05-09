package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.RateApi
import com.kyberswap.android.data.db.RateDao
import com.kyberswap.android.data.mapper.RateMapper
import com.kyberswap.android.domain.model.Rate
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.util.TokenClient
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.math.pow


class TokenDataRepository @Inject constructor(
    private val tokenClient: TokenClient,
    private val api: RateApi,
    private val rateMapper: RateMapper,
    private val rateDao: RateDao,
    private val context: Context
) :
    TokenRepository {
    override fun getMarketRate(param: GetMarketRateUseCase.Param): Flowable<String> {

        return Flowable.mergeDelayError(rateDao.all, api.getRate().map { it.data }.toFlowable()
            .flatMapIterable { rate -> rate }
            .map { rateMapper.transform(it) }
            .toList()
            .doAfterSuccess {
                rateDao.insertRates(it)
    .toFlowable()
        )
            .flatMapIterable { rate -> rate }
            .filter { rate ->
                rate.source == param.sourceToken && rate.dest == param.destToken
    .map {
                it.rate
    

//        return if (rateDao.all.blockingGet().isNullOrEmpty()) {
//            api.getRate()
//                .map { it.data }
//                .toFlowable()
//                .flatMapIterable { token -> token }
//                .map { rateMapper.transform(it) }
//                .toList()
//                .doAfterSuccess {
//                    rateDao.insertRates(it)
//        
//                .map {
//                    val first = it.firstOrNull { rate ->
//                        rate.source == param.sourceToken && rate.dest == param.destToken
//            
//                    first?.rate ?: "0"
//        
// else {
//            Single.fromCallable {
//                rateDao.all.filter {
//
//        
//                rateDao.getRateForTokenPair(param.sourceToken, param.destToken)
//    .map {
//                it.rate
//    
//
//
//


    }

    private fun fetchMarketRate(): Single<List<Rate>> {
        return api.getRate().map { it.data }
            .toFlowable()
            .flatMapIterable { token -> token }
            .map { rateMapper.transform(it) }
            .toList()
            .doAfterSuccess {
                rateDao.insertRates(it)
    
    }

    override fun getExpectedRate(param: GetExpectedRateUseCase.Param): Single<List<String>> {
        val sourceToken = param.swap.tokenSource
        val amount = 10.0.pow(sourceToken.tokenDecimal).times(param.srcAmount.toDouble())
            .toBigDecimal().toBigInteger()
        return Single.fromCallable {
            val expectedRate = tokenClient.getExpectedRate(
                param.walletAddress,
                context.getString(R.string.kyber_address),
                sourceToken.tokenAddress,
                param.swap.tokenDest.tokenAddress,
                amount
            )
            expectedRate

    }


    companion object {
        const val ETH = "ETH"
        const val USD = "USD"
        private const val COUNTER_START = 1
        private const val ATTEMPTS = 5
    }
}