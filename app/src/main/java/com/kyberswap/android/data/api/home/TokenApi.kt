package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.data.api.token.TokenPriceEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TokenApi {
    @GET("change24h")
    fun getChange24h(): Single<Map<String, TokenEntity>>

    @GET("token_price")
    fun tokenPrice(
        @Query("currency") currency: String
    ): Single<TokenPriceEntity>

}