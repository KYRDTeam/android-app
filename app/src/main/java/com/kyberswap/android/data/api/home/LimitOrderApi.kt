package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.limitorder.CancelledEntity
import com.kyberswap.android.data.api.limitorder.EligibleAddressEntity
import com.kyberswap.android.data.api.limitorder.FavoritePairsEntity
import com.kyberswap.android.data.api.limitorder.FeeEntity
import com.kyberswap.android.data.api.limitorder.LimitOrderResponseEntity
import com.kyberswap.android.data.api.limitorder.ListLimitOrderResponseEntity
import com.kyberswap.android.data.api.limitorder.NonceEntity
import com.kyberswap.android.data.api.limitorder.PendingBalancesEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LimitOrderApi {
    @GET("api/orders")
    fun getOrders(
        @Query("page_index") pageIndex: Int
    ): Single<ListLimitOrderResponseEntity>

    @GET("api/orders/fee")
    fun getFee(
        @Query("src") sourceAddress: String,
        @Query("dst") destAddress: String,
        @Query("src_amount") srcAmount: String,
        @Query("dst_amount") dstAmount: String,
        @Query("user_addr") userAddr: String
    ): Single<FeeEntity>

    @GET("api/orders/related_orders")
    fun getRelatedOrders(
        @Query("user_addr") address: String,
        @Query("src") src: String,
        @Query("dst") dest: String,
        @Query("min_rate") minRate: String?
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
        @Field("signature") signature: String,
        @Field("side_trade") sideTrade: String?

    ): Single<LimitOrderResponseEntity>

    @PUT("api/orders/{id}/cancel")
    fun cancelOrder(@Path("id") id: Long): Single<CancelledEntity>

    @GET("api/orders/pending_balances")
    fun getPendingBalances(
        @Query("user_addr") address: String
    ): Single<PendingBalancesEntity>

    @GET("api/orders/eligible_address")
    fun eligibleAddress(
        @Query("user_addr") address: String
    ): Single<EligibleAddressEntity>

    @GET("api/orders/favorite_pairs")
    fun getFavoritePairs(): Single<FavoritePairsEntity>

    @Headers("Content-Type: application/json")
    @PUT("api/orders/favorite_pair")
    fun favPair(@Body body: RequestBody): Single<ResponseStatusEntity>
}