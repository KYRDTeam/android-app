package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.gas.GasPriceEntity
import com.kyberswap.android.data.api.rate.MarketRateEntity
import io.reactivex.Single
import retrofit2.http.GET

interface SwapApi {
    @GET("rate")
    fun getRate(): Single<MarketRateEntity>

    @GET("gasPrice")
    fun getGasPrice(): Single<GasPriceEntity>
}