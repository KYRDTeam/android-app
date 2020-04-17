package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.gas.GasLimitEntity
import com.kyberswap.android.data.api.limitorder.MarketEntity
import com.kyberswap.android.data.api.rate.ExpectedRateEntity
import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.data.api.token.TokenPriceEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigInteger

interface TokenApi {
    @GET("change24h")
    fun getChange24h(): Single<Map<String, TokenEntity>>

    @GET("token_price")
    fun tokenPrice(
        @Query("currency") currency: String
    ): Single<TokenPriceEntity>

    @GET("gas_limit")
    fun estimateGas(
        @Query("source")
        sourceTokenAddress: String,
        @Query("dest")
        destTokenAddress: String,
        @Query("amount")
        sourceAmount: String
    ): Single<GasLimitEntity>

    @GET("pairs/market")
    fun getPairMarket(): Single<MarketEntity>

    @GET("expectedRate")
    fun getExpectedRate(
        @Query("source")
        srcAddress: String,
        @Query("dest")
        dstAddress: String,
        @Query("sourceAmount")
        amount: BigInteger
    ): Single<ExpectedRateEntity>
}