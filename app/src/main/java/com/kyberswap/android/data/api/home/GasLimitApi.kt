package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.gas.GasLimitEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GasLimitApi {

    @GET("gas_limit")
    fun estimateGas(
        @Query("source")
        sourceTokenAddress: String,
        @Query("dest")
        destTokenAddress: String,
        @Query("amount")
        sourceAmount: String
    ): Single<GasLimitEntity>
}