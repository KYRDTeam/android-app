package com.kyberswap.android.domain.repository

import com.kyberswap.android.data.api.chart.Data
import com.kyberswap.android.domain.model.Chart
import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import com.kyberswap.android.domain.usecase.token.GetChartDataForTokenUseCase
import com.kyberswap.android.domain.usecase.token.GetToken24hVolUseCase
import com.kyberswap.android.domain.usecase.token.SaveTokenUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface TokenRepository {
    fun getExpectedRate(param: GetExpectedRateUseCase.Param): Flowable<List<String>>

    fun getMarketRate(param: GetMarketRateUseCase.Param): Flowable<String>

    fun getChartData(param: GetChartDataForTokenUseCase.Param): Single<Chart>

    fun get24hVol(param: GetToken24hVolUseCase.Param): Single<Data>

    fun saveToken(param: SaveTokenUseCase.Param): Completable

}