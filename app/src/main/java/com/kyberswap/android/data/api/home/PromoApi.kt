package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.promo.PromoResponseEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PromoApi {
    @GET("api/promo")
    fun getPromo(
        @Header("signed") hash: String,
        @Query("isInternalApp") isInternalApp: String,
        @Query("code") code: String,
        @Query("nonce") nonce: Long
    ): Single<PromoResponseEntity>

}