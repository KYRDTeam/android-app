package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.rate.MarketRateEntity
import io.reactivex.Single
import retrofit2.http.GET

interface RateApi {
    @GET("rate")
    fun getRate(): Single<MarketRateEntity>
}