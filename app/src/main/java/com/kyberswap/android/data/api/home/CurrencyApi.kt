package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.currencies.CurrencyEntity
import io.reactivex.Single
import retrofit2.http.GET

interface CurrencyApi {
    @GET("api/currencies")
    fun internalCurrencies(): Single<CurrencyEntity>
}