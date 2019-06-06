package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.limitorder.LimitOrderResponse
import io.reactivex.Single
import retrofit2.http.GET

interface LimitOrderApi {
    @GET("api/orders")
    fun getOrders(): Single<LimitOrderResponse>
}