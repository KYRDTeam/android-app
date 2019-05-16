package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.db.RateDao
import com.kyberswap.android.data.mapper.RateMapper
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.updatePrecision
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.pow


class TokenDataRepository @Inject constructor(
    private val tokenClient: TokenClient,
    private val api: SwapApi,
    private val rateMapper: RateMapper,
    private val rateDao: RateDao,
    private val context: Context
) :
    TokenRepository {
    override fun getMarketRate(param: GetMarketRateUseCase.Param): Flowable<String> {
        return Flowable.mergeDelayError(
            rateDao.all,
            api.getRate()
                .map { it.data }
                .map { rateMapper.transform(it) }
                .doAfterSuccess {
                    rateDao.updateAll(it)
                }
                .toFlowable()
        )
            .map { rates ->
                val sourceTokenToEtherRate =
                    rates.firstOrNull { it.source == param.sourceToken && it.dest == ETH }
                val etherToDestTokenRate =
                    rates.firstOrNull { it.source == ETH && it.dest == param.destToken }
                sourceTokenToEtherRate?.rate?.updatePrecision().toBigDecimalOrDefaultZero()
                    .multiply(
                        etherToDestTokenRate?.rate?.updatePrecision().toBigDecimalOrDefaultZero()
                    ).toPlainString()
            }

    }

    override fun getExpectedRate(param: GetExpectedRateUseCase.Param): Flowable<List<String>> {
        val sourceToken = param.swap.tokenSource
        val amount = 10.0.pow(sourceToken.tokenDecimal).times(param.srcAmount.toDouble())
            .toBigDecimal().toBigInteger()
        return Flowable.fromCallable {
            val expectedRate = tokenClient.getExpectedRate(
                param.walletAddress,
                context.getString(R.string.kyber_address),
                sourceToken.tokenAddress,
                param.swap.tokenDest.tokenAddress,
                amount
            )
            expectedRate
        }
            .repeatWhen {
                it.delay(20, TimeUnit.SECONDS)
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

    companion object {
        const val ETH = "ETH"
        const val USD = "USD"
        private const val COUNTER_START = 1
        private const val ATTEMPTS = 5
    }
}