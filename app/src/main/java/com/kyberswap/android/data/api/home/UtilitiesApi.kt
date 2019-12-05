package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.gas.GasLimitEntity
import com.kyberswap.android.data.api.token.QuoteAmountEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface UtilitiesApi {

    @GET("gas_limit")
    fun estimateGas(
        @Query("source")
        sourceTokenAddress: String,
        @Query("dest")
        destTokenAddress: String,
        @Query("amount")
        sourceAmount: String
    ): Single<GasLimitEntity>

    @GET("quote_amount")
    fun sourceAmount(
        @Query("quote") source: String,
        @Query("base") dest: String,
        @Query("base_amount") destAmount: String,
        @Query("type") type: String
    ): Single<QuoteAmountEntity>
}