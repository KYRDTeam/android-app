package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.limitorder.*
import io.reactivex.Single
import retrofit2.http.*

interface LimitOrderApi {
    @GET("api/orders")
    fun getOrders(
        @Query("user_address") userAddress: String
    ): Single<ListLimitOrderResponseEntity>

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
        @Query("user_address") address: String,
        @Query("src_token") src: String,
        @Query("dest_token") dest: String,
        @Query("status") status: String?
    ): Single<ListLimitOrderResponseEntity>

    @GET("api/orders/nonce")
    fun getNonce(
        @Query("userAddress")
        address: String,
        @Query("src")
        srcAddress: String,
        @Query("dest")
        destAddress: String
    ): Single<NonceEntity>

    @POST("api/orders")
    @FormUrlEncoded
    fun createOrder(
        @Field("user_address") userAddress: String,
        @Field("nonce") nonce: String,
        @Field("src_token") srcToken: String,
        @Field("dest_token") destToken: String,
        @Field("src_amount") srcAmount: String,
        @Field("min_rate") minRate: String,
        @Field("dest_address") destAddress: String,
        @Field("fee") fee: String,
        @Field("signature") signature: String

    ): Single<LimitOrderResponseEntity>

    @PUT("api/orders/{id}/cancel")
    fun cancelOrder(@Path("id") id: Long): Single<CancelledEntity>
}