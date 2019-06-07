package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.limitorder.FeeEntity
import com.kyberswap.android.data.api.limitorder.LimitOrderResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface LimitOrderApi {
    @GET("api/orders")
    fun getOrders(): Single<LimitOrderResponse>

    @GET("api/orders/fee")
    fun getFee(
        @Query("src") sourceAddress: String,
        @Query("dst") destAddress: String,
        @Query("src_amount") srcAmount: String,
        @Query("dst_amount") dstAmount: String,
        @Query("user_addr") userAddr: String
    ): Single<FeeEntity>

    @GET("api/orders")
    fun getRelatedOrders(
        @Query("addr") address: String,
        @Query("src") src: String,
        @Query("dst") dest: String,
        @Query("status") status: String
    ): Single<LimitOrderResponse>
}