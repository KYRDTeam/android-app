package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import io.reactivex.Flowable
import io.reactivex.Single

interface TokenRepository {
    fun getExpectedRate(param: GetExpectedRateUseCase.Param): Single<List<String>>

    fun getMarketRate(param: GetMarketRateUseCase.Param): Flowable<String>
}