package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.token.Token
import io.reactivex.Flowable
import io.reactivex.Single

interface BalanceRepository {
    fun getChange24h(owner: String): Flowable<Token>
    fun getBalance(owner: String, tokenList: List<Token>): Single<List<Token>>
    fun getBalance(owner: String, token: Token): Single<Token>
}
