package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.api.chart.Data
import com.kyberswap.android.data.api.home.ChartApi
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.api.rate.ReferencePriceEntity
import com.kyberswap.android.data.db.RateDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.mapper.ChartMapper
import com.kyberswap.android.data.mapper.RateMapper
import com.kyberswap.android.domain.model.Chart
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateSequentialUseCase
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.domain.usecase.token.GetChartDataForTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetToken24hVolUseCase
import com.kyberswap.android.domain.usecase.token.SaveTokenUseCase
import com.kyberswap.android.presentation.common.isKatalyst
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntSafe
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
import com.kyberswap.android.util.ext.updatePrecision
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Flowables
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.pow

class TokenDataRepository @Inject constructor(
    private val tokenClient: TokenClient,
    private val tokenApi: TokenApi,
    private val swapApi: SwapApi,
    private val chartApi: ChartApi,
    private val rateDao: RateDao,
    private val tokenDao: TokenDao,
    private val rateMapper: RateMapper,
    private val chartMapper: ChartMapper,
    private val userApi: UserApi,
    private val context: Context
) :
    TokenRepository {

    private fun getReferencePrice(base: String): Single<ReferencePriceEntity> {
        return if (base.equals(Token.ETH, true)) {
            Single.fromCallable {
                ReferencePriceEntity(true, 1.toString())
            }
        } else {
            swapApi.getReferencePrice(base, Token.ETH)
        }
    }

    override fun getMarketRate(param: GetMarketRateUseCase.Param): Flowable<String> {
        return Flowables.zip(
            getReferencePrice(param.src).toFlowable(),
            getReferencePrice(param.dest).toFlowable()
        ) { srcRate, destRate ->
            val destValue = destRate.value.toBigDecimalOrDefaultZero()
            if (srcRate.success == true &&
                destRate.success == true &&
                destValue != BigDecimal.ZERO
            ) {
                srcRate.value.toBigDecimalOrDefaultZero().divide(destValue, 18, RoundingMode.UP)
                    .toPlainString()
            } else {
                throw RuntimeException("don't have rate")
            }
        }.onErrorResumeNext(Flowable.mergeDelayError(
            rateDao.all,
            swapApi.getRate()
                .map { it.data }
                .map { rateMapper.transform(it) }
                .doAfterSuccess {
                    rateDao.updateAll(it)
                }
                .toFlowable()
        )
            .map { rates ->
                val sourceTokenToEtherRate =
                    rates.firstOrNull { it.source == param.src && it.dest == Token.ETH }

                val srcToEtherRateValue =
                    if ((sourceTokenToEtherRate == null ||
                                sourceTokenToEtherRate.rate.toBigIntSafe() == BigInteger.ZERO)
                        && (param.src == Token.ETH_SYMBOL || param.src == Token.WETH_SYMBOL)
                    ) {
                        BigDecimal.ONE
                    } else {
                        sourceTokenToEtherRate?.rate?.updatePrecision().toBigDecimalOrDefaultZero()
                    }

                val etherToDestTokenRate =
                    rates.firstOrNull { it.source == Token.ETH && it.dest == param.dest }

                val etherToDestRateValue =
                    if ((etherToDestTokenRate == null ||
                                etherToDestTokenRate.rate.toBigIntSafe() == BigInteger.ZERO)
                        && (param.dest == Token.ETH_SYMBOL || param.dest == Token.WETH_SYMBOL)
                    ) {
                        BigDecimal.ONE
                    } else {
                        etherToDestTokenRate?.rate?.updatePrecision().toBigDecimalOrDefaultZero()
                    }
                srcToEtherRateValue
                    .multiply(
                        etherToDestRateValue
                    ).toPlainString()
            })
    }

    override fun getExpectedRate(param: GetExpectedRateSequentialUseCase.Param): Single<List<String>> {
        val tokenSource = param.tokenSource
        val tokenDest = param.tokenDest
        val isETHWETHPair =
            (tokenSource.isETH || tokenSource.isWETH || tokenSource.isETHWETH) && (tokenDest.isETH || tokenDest.isWETH || tokenDest.isETHWETH)
        val amount =
            10.0.pow(tokenSource.tokenDecimal).times(param.srcAmount.toDoubleOrDefaultZero())
                .toBigDecimal().toBigInteger()
        val platformFee = if (isETHWETHPair) BigInteger.ZERO else param.platformFee.toBigInteger()
        return getHint(
            tokenSource.tokenAddress,
            tokenDest.tokenAddress,
            param.srcAmount,
            param.isReserveRouting
        ).flatMap { hint ->
            Single.fromCallable {
                val expectedRate = tokenClient.getExpectedRate(
                    context.getString(R.string.kyber_address),
                    tokenSource,
                    tokenDest,
                    amount,
                    platformFee,
                    hint
                )
                expectedRate
            }
        }
    }

    private fun getHint(
        srcAddress: String,
        destAddress: String,
        amount: String,
        isReserveRouting: Boolean
    ): Single<String?> {
        return if (isReserveRouting) {
            userApi.getHint(srcAddress, destAddress, amount).map {
                if (it.success) {
                    it.hint ?: SwapDataRepository.DEFAULT_HINT
                } else {
                    SwapDataRepository.DEFAULT_HINT
                }
            }.onErrorReturnItem(SwapDataRepository.DEFAULT_HINT)
        } else {
            Single.fromCallable {
                SwapDataRepository.DEFAULT_HINT
            }
        }
    }

    override fun getExpectedRate(param: GetExpectedRateUseCase.Param): Flowable<List<String>> {
        val tokenSource = param.tokenSource
        val tokenDest = param.tokenDest

        val isETHWETHPair =
            (tokenSource.isETH || tokenSource.isWETH || tokenSource.isETHWETH) && (tokenDest.isETH || tokenDest.isWETH || tokenDest.isETHWETH)

        val amount =
            10.0.pow(tokenSource.tokenDecimal).times(param.srcAmount.toDoubleOrDefaultZero())
                .toBigDecimal().toBigInteger()
        return tokenApi.getExpectedRate(tokenSource.tokenAddress, tokenDest.tokenAddress, amount)
            .map {
                if (it.error) {
                    throw RuntimeException("Can not get rate from: " + context.getString(R.string.token_endpoint_url) + "expectedRate")
                } else {
                    listOf(
                        getExpectedRateAfterFee(
                            it.expectedRate,
                            if (isETHWETHPair) 0 else param.platFormFee
                        )
                    )
                }
            }.repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }

//        return Flowable.fromCallable {
//            val expectedRate = tokenClient.getExpectedRate(
//                context.getString(R.string.kyber_address),
//                tokenSource,
//                tokenDest,
//                amount
//            )
//            expectedRate
//        }
//            .repeatWhen {
//                it.delay(15, TimeUnit.SECONDS)
//            }
//            .retryWhen { throwable ->
//                throwable.compose(zipWithFlatMap())
//            }
    }

    private fun getExpectedRateAfterFee(expectedRate: String, bps: Int): String {
        return if (isKatalyst) {
            Convert.fromWei(
                expectedRate.toBigDecimalOrDefaultZero()
                    .multiply(
                        BigDecimal.ONE - bps.toBigDecimal()
                            .divide(10000.toBigDecimal(), 18, RoundingMode.UP)
                    ),
                Convert.Unit.ETHER
            ).toPlainString()
        } else {
            Convert.fromWei(
                expectedRate.toBigDecimalOrDefaultZero(),
                Convert.Unit.ETHER
            ).toPlainString()
        }
    }

    override fun getChartData(param: GetChartDataForTokenUseCase.Param): Single<Chart> {
        val to = System.currentTimeMillis() / 1000
        val from = param.charType.fromTime(to)

        return chartApi.getChartHistory(
            param.symbol,
            param.charType.resolution,
            param.rateType,
            from,
            to
        )
            .map { chartMapper.transform(it) }
    }

    override fun get24hVol(param: GetToken24hVolUseCase.Param): Single<Data> {
        return chartApi.get24hVol().map {
            it.data
        }
            .toFlowable()
            .flatMapIterable { tokenCurrency -> tokenCurrency }
            .filter {
                it.baseSymbol == param.token.tokenSymbol
            }
            .first(Data())
//            .map {
//                it.eth24hVolume?.toDisplayNumber() ?: ""
//            }
    }

    override fun saveToken(param: SaveTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val local = tokenDao.getTokenByAddress(param.token.tokenAddress)
            local?.let {
                val favToken = local.copy(fav = param.token.fav)
                tokenDao.updateToken(favToken)
            }

        }
    }
}