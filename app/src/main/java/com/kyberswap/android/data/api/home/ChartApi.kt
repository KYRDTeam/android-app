package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.chart.ChartResponseEntity
import com.kyberswap.android.data.api.chart.MarketEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ChartApi {
    @GET("chart/history")
    fun getChartHistory(
        @Query("symbol")
        tokenSymbol: String,
        @Query("resolution")
        resolution: String,
        @Query("rateType")
        rateType: String,
        @Query("from")
        from: Long,
        @Query("to")
        to: Long
    ): Single<ChartResponseEntity>

    @GET("market")
    fun get24hVol(): Single<MarketEntity>
}