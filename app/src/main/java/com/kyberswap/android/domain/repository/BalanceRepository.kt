package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.usecase.balance.UpdateBalanceUseCase
import com.kyberswap.android.domain.usecase.token.GetBalancePollingUseCase
import com.kyberswap.android.domain.usecase.token.GetTokensBalanceUseCase
import com.kyberswap.android.domain.usecase.token.PrepareBalanceUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface BalanceRepository {
    fun getChange24hPolling(param: GetBalancePollingUseCase.Param): Flowable<List<Token>>
    fun getChange24h(): Flowable<List<Token>>
    fun getTokenBalance(token: Token): Completable
    fun getTokenBalances(param: GetTokensBalanceUseCase.Param): Completable
    fun getBalance(param: PrepareBalanceUseCase.Param = PrepareBalanceUseCase.Param()): Single<List<Token>>
    fun updateBalance(param: UpdateBalanceUseCase.Param): Completable
}
