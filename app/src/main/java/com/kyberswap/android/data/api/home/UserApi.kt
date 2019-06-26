package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.alert.AlertResponseEntity
import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.UserStatusEnity
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @FormUrlEncoded
    @POST("api/users/signup")
    fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String,
        @Field("display_name") displayName: String,
        @Field("subscription") subscription: Boolean
    ): Single<UserStatusEnity>


    @FormUrlEncoded
    @POST("api/users/reset_password")
    fun resetPassword(
        @Field("email") email: String
    ): Single<UserStatusEnity>


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

    @GET("/api/alerts")
    fun getAlert(): Single<AlertResponseEntity>
}