package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.usecase.swap.GetExpectedRateUseCase
import com.kyberswap.android.domain.usecase.swap.GetMarketRateUseCase
import io.reactivex.Flowable

interface TokenRepository {
    fun getExpectedRate(param: GetExpectedRateUseCase.Param): Flowable<List<String>>

    fun getMarketRate(param: GetMarketRateUseCase.Param): Flowable<String>
}