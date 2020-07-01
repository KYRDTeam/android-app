package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.currencies.CurrencyEntity
import com.kyberswap.android.data.api.fee.PlatformFeeEntity
import io.reactivex.Single
import retrofit2.http.GET

interface KyberSwapApi {
    @GET("api/currencies")
    fun internalCurrencies(): Single<CurrencyEntity>

    @GET("api/swap_fee")
    fun swapFee(): Single<PlatformFeeEntity>
}