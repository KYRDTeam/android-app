package com.kyberswap.android.data.api.home

import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.data.api.alert.AlertMethodsResponseEntity
import com.kyberswap.android.data.api.alert.AlertResponseEntity
import com.kyberswap.android.data.api.alert.LeaderBoardEntity
import com.kyberswap.android.data.api.campaign.CampaignResponseEntity
import com.kyberswap.android.data.api.cap.CapEntity
import com.kyberswap.android.data.api.notification.NotificationsResponseEntity
import com.kyberswap.android.data.api.notification.SubscriptionNotificationEntity
import com.kyberswap.android.data.api.reserve.HintEntity
import com.kyberswap.android.data.api.user.DataTransferStatusEntity
import com.kyberswap.android.data.api.user.KycResponseStatusEntity
import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import com.kyberswap.android.data.api.user.UserInfoEntity
import com.kyberswap.android.data.api.wallet.EligibleWalletStatusEntity
import com.kyberswap.android.domain.model.ResponseStatus
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
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
        @Field("password") password: String,
        @Field("two_factor_code") twoFactorCode: String?

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
        @Field("confirm_signup") confirmSignUp: Boolean,
        @Field("two_factor_code") twoFactorCode: String?
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

    @GET("api/alerts/campaign_prizes")
    fun getCampaignResult(): Single<LeaderBoardEntity>

    @POST("api/kyc_profile/personal_info")
    @FormUrlEncoded
    fun savePersonalInfo(
        @Field("kyc_profile[first_name]") firstName: String,
        @Field("kyc_profile[middle_name]") middleName: String,
        @Field("kyc_profile[last_name]") lastName: String,
        @Field("kyc_profile[native_full_name]") fullName: String?,
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
        @Field("kyc_profile[tax_indentification_number]") taxIdentificationNumber: String?,
        @Field("kyc_profile[source_fund]") sourceFund: String

    ): Single<KycResponseStatusEntity>

    @POST("api/kyc_profile/identity_info")
    @FormUrlEncoded
    fun saveIdentityInfo(
        @Field("kyc_profile[document_id]") documentId: String,
        @Field("kyc_profile[document_type]") documentType: String,
        @Field("kyc_profile[document_issue_date]") documentIssueDate: String?,
        @Field("kyc_profile[issue_date_non_applicable]") isIssueDateNoneApplicable: Boolean?,
        @Field("kyc_profile[document_expiry_date]") documentExpiredDate: String?,
        @Field("kyc_profile[expiry_date_non_applicable]") isExpiredDateNoneApplicable: Boolean?,
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

    @POST("api/users/player_id")
    @FormUrlEncoded
    fun updatePushToken(
        @Field("player_id") playerId: String
    ): Single<ResponseStatusEntity>

    @GET("api/user_stats")
    fun getUserStats(
        @Query("address")
        address: String
    ): Single<CapEntity>

    @GET("api/get_alert_methods")
    fun getAlertMethod(): Single<AlertMethodsResponseEntity>

    @Headers("Content-Type: application/json")
    @POST("api/update_alert_methods")
    fun updateAlertMethods(@Body rawJsonString: RequestBody): Single<ResponseStatusEntity>

    @POST("api/transactions")
    @FormUrlEncoded
    fun submitTx(
        @Field("tx_hash") tx: String
    ): Single<ResponseStatusEntity>

    @GET("api/notifications")
    fun getNotifications(
        @Query("page_index") pageIndex: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): Single<NotificationsResponseEntity>

    @GET("api/wallet/screening")
    fun checkEligibleWallet(
        @Query("wallet") walletAddress: String
    ): Single<EligibleWalletStatusEntity>

    @Headers("Content-Type: application/json")
    @PUT("api/notifications/mark_as_read")
    fun markAsRead(@Body rawJsonString: RequestBody): Single<ResponseStatusEntity>

    @POST("api/users/transfer_aggreement")
    @FormUrlEncoded
    fun transferAgreement(@Field("transfer_permission") action: String): Single<DataTransferStatusEntity>

    @DELETE("api/alerts/delete_triggered")
    fun deleteAllTriggerAlerts(): Single<ResponseStatusEntity>

    @GET("api/users/subscription_tokens")
    fun getSubscriptionNotifications(): Single<SubscriptionNotificationEntity>

    @PATCH("api/users/toggle_price_noti")
    @FormUrlEncoded
    fun togglePriceNoti(
        @Field("price_noti") param: Boolean
    ): Single<ResponseStatus>

    @Headers("Content-Type: application/json")
    @POST("api/users/subscription_tokens")
    fun updateSubscribedTokens(@Body rawJsonString: RequestBody): Single<ResponseStatusEntity>

    @GET("api/mobile_banners")
    fun getCampaigns(): Single<CampaignResponseEntity>

    @GET("api/swap_hint")
    fun getHint(
        @Query("src")
        sourceTokenAddress: String,
        @Query("dst")
        destTokenAddress: String,
        @Query("amount")
        sourceAmount: String
    ): Single<HintEntity>
}