package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.currencies.CurrencyEntity
import com.kyberswap.android.data.api.fee.PlatformFeeEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface KyberSwapApi {
    @GET("api/currencies")
    fun internalCurrencies(): Single<CurrencyEntity>

    @GET("api/swap_fee")
    fun swapFee(
        @Query("src") sourceAddress: String,
        @Query("dst") destAddress: String
    ): Single<PlatformFeeEntity>
}