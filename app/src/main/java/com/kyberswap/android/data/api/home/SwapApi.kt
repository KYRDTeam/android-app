package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.cap.CapEntity
import com.kyberswap.android.data.api.gas.GasPriceEntity
import com.kyberswap.android.data.api.gas.MaxGasPriceEntity
import com.kyberswap.android.data.api.rate.MarketRateEntity
import com.kyberswap.android.data.api.token.KyberEnabledEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SwapApi {
    @GET("rate")
    fun getRate(): Single<MarketRateEntity>

    @GET("gasPrice")
    fun getGasPrice(): Single<GasPriceEntity>

    @GET("maxGasPrice")
    fun getMaxGasPrice(): Single<MaxGasPriceEntity>

    @GET("users")
    fun getCap(
        @Query("address") address: String?
    ): Single<CapEntity>

    @GET("kyberEnabled")
    fun getKyberEnabled(): Single<KyberEnabledEntity>

}