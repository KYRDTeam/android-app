package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.cap.CapEntity
import com.kyberswap.android.data.api.gas.GasPriceEntity
import com.kyberswap.android.data.api.rate.MarketRateEntity
import com.kyberswap.android.data.api.token.QuoteAmountEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SwapApi {
    @GET("rate")
    fun getRate(): Single<MarketRateEntity>

    @GET("gasPrice")
    fun getGasPrice(): Single<GasPriceEntity>

    @GET("users")
    fun getCap(
        @Query("address") address: String?
    ): Single<CapEntity>

    @GET("quote_amount")
    fun sourceAmount(
        @Query("quote") source: String,
        @Query("base") dest: String,
        @Query("base_amount") destAmount: String,
        @Query("type") type: String
    ): Single<QuoteAmountEntity>
}