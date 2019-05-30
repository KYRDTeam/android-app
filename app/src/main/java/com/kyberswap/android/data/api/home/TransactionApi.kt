package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.transaction.TransactionsEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionApi {
    @GET("api")
    fun getTransaction(
        @Query("module") module: String,
        @Query("action") action: String,
        @Query("address") address: String,
        @Query("sort") sort: String,
        @Query("apikey") apikey: String
    ): Single<TransactionsEntity>
}