package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.data.api.alert.AlertResponseEntity
import com.kyberswap.android.data.api.alert.LeaderBoardEntity
import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import io.reactivex.Single
import retrofit2.http.*
import java.math.BigDecimal

interface UserApi {
    @FormUrlEncoded
    @POST("api/users/signup")
    fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String,
        @Field("display_name") displayName: String,
        @Field("subscription") subscription: Boolean
    ): Single<ResponseStatusEntity>


    @FormUrlEncoded
    @POST("api/users/reset_password")
    fun resetPassword(
        @Field("email") email: String
    ): Single<ResponseStatusEntity>


    @FormUrlEncoded
    @POST("api/users/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Single<LoginUserEntity>


    @FormUrlEncoded
    @POST("api/users/social_login")
    fun socialLogin(
        @Field("type") type: String,
        @Field("access_token") accessToken: String?,
        @Field("subscription") subscription: Boolean,
        @Field("photo_url") photoUrl: String?,
        @Field("two_factor_code") twoFa: String?,
        @Field("display_name") displayName: String?,
        @Field("oauth_token") oauthToken: String?,
        @Field("oauth_token_secret") oauthTokenSecret: String?,
        @Field("confirm_signup") confirmSignUp: Boolean
    ): Single<LoginUserEntity>

    @GET("api/alerts")
    fun getAlert(): Single<AlertResponseEntity>

    @POST("api/alerts")
    @FormUrlEncoded
    fun createAlert(
        @Field("base") base: Int,
        @Field("symbol") symbol: String,
        @Field("alert_price") alertPrice: BigDecimal,
        @Field("is_above") isAbove: Boolean

    ): Single<AlertEntity>

    @PUT("api/alerts/{id}")
    @FormUrlEncoded
    fun updateAlert(
        @Path("id") id: Long,
        @Field("base") base: Int,
        @Field("symbol") symbol: String,
        @Field("alert_price") alertPrice: BigDecimal,
        @Field("is_above") isAbove: Boolean

    ): Single<AlertEntity>

    @DELETE("api/alerts/{id}")
    fun deleteAlert(@Path("id") id: Long): Single<ResponseStatusEntity>

    @GET("api/alerts/ranks")
    fun getLeaderBoard(): Single<LeaderBoardEntity>

}