package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import io.reactivex.Flowable
import io.reactivex.Single

interface BalanceRepository {
    fun getChange24hPolling(owner: String): Flowable<List<Token>>
    fun getChange24h(): Flowable<List<Token>>
    fun getBalance(param: PrepareBalanceUseCase.Param): Single<List<Token>>
}
