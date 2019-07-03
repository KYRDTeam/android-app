package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.data.api.alert.AlertResponseEntity
import com.kyberswap.android.data.api.alert.LeaderBoardEntity
import com.kyberswap.android.data.api.user.KycResponseStatusEntity
import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import com.kyberswap.android.data.api.user.UserInfoEntity
import io.reactivex.Single
import retrofit2.Response
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
    fun deleteAlert(@Path("id") id: Long): Single<Response<Void>>

    @GET("api/alerts/ranks")
    fun getLeaderBoard(): Single<LeaderBoardEntity>

    @POST("api/kyc_profile/personal_info")
    @FormUrlEncoded
    fun savePersonalInfo(
        @Field("kyc_profile[first_name]") firstName: String,
        @Field("kyc_profile[last_name]") lastName: String,
        @Field("kyc_profile[nationality]") nationality: String,
        @Field("kyc_profile[country]") country: String,
        @Field("kyc_profile[dob]") dob: String,
        @Field("kyc_profile[gender]") gender: Int,
        @Field("kyc_profile[residential_address]") residentialAddress: String,
        @Field("kyc_profile[city]") city: String,
        @Field("kyc_profile[zip_code]") zipCode: String,
        @Field("kyc_profile[document_proof_address]") docProofAddress: String,
        @Field("kyc_profile[photo_proof_address]") photoProofAddress: String,
        @Field("kyc_profile[occupation_code]") occupationCode: String?,
        @Field("kyc_profile[industry_code]") industryCode: String?,
        @Field("kyc_profile[tax_residency_country]") taxResidencyCountry: String?,
        @Field("kyc_profile[have_tax_indentification]") haveTaxIdentification: Int?,
        @Field("kyc_profile[tax_indentification_number]") taxIdentificationNumber: String?

    ): Single<KycResponseStatusEntity>

    @POST("api/kyc_profile/identity_info")
    @FormUrlEncoded
    fun saveIdentityInfo(
        @Field("kyc_profile[document_id]") documentId: String,
        @Field("kyc_profile[document_type]") documentType: String,
        @Field("kyc_profile[document_issue_date]") documentIssueDate: String,
        @Field("kyc_profile[photo_selfie]") photoSelfie: String,
        @Field("kyc_profile[photo_identity_front_side]") photoFrontSide: String,
        @Field("kyc_profile[photo_identity_back_side]") photoBackSide: String

    ): Single<KycResponseStatusEntity>

    @GET("api/users/get_info")
    fun getUserInfo(): Single<UserInfoEntity>

    @POST("api/kyc_profile/submit_kyc")
    fun submit(): Single<KycResponseStatusEntity>

    @POST("api/kyc_profile/resubmit_kyc")
    fun resubmit(): Single<KycResponseStatusEntity>

}