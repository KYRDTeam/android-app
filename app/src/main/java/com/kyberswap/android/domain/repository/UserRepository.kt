package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.AlertMethodsResponse
import com.kyberswap.android.domain.model.DataTransferStatus
import com.kyberswap.android.domain.model.KycResponseStatus
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.RatingInfo
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.usecase.alert.UpdateAlertMethodsUseCase
import com.kyberswap.android.domain.usecase.notification.ReadNotificationsUseCase
import com.kyberswap.android.domain.usecase.profile.Base64DecodeUseCase
import com.kyberswap.android.domain.usecase.profile.DataTransferUseCase
import com.kyberswap.android.domain.usecase.profile.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.profile.LoginUseCase
import com.kyberswap.android.domain.usecase.profile.ReSubmitUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.ResetPasswordUseCase
import com.kyberswap.android.domain.usecase.profile.ResizeImageUseCase
import com.kyberswap.android.domain.usecase.profile.SaveIdPassportUseCase
import com.kyberswap.android.domain.usecase.profile.SaveKycInfoUseCase
import com.kyberswap.android.domain.usecase.profile.SaveLocalPersonalInfoUseCase
import com.kyberswap.android.domain.usecase.profile.SavePersonalInfoUseCase
import com.kyberswap.android.domain.usecase.profile.SaveRatingInfoUseCase
import com.kyberswap.android.domain.usecase.profile.SignUpUseCase
import com.kyberswap.android.domain.usecase.profile.SubmitUserInfoUseCase
import com.kyberswap.android.domain.usecase.profile.UpdatePushTokenUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface UserRepository {
    fun signUp(param: SignUpUseCase.Param): Single<ResponseStatus>

    fun login(param: LoginUseCase.Param): Single<LoginUser>

    fun loginSocial(param: LoginSocialUseCase.Param): Single<LoginUser>

    fun resetPassword(param: ResetPasswordUseCase.Param): Single<ResponseStatus>

    fun getUser(): Flowable<UserInfo>

    fun userInfo(): Single<UserInfo?>

    fun getUserInfo(): Flowable<UserInfo>

    fun refreshUserInfo(): Single<UserInfo>

    fun fetchUserInfo(): Flowable<UserInfo>

    fun pollingUserInfo(): Flowable<UserInfo>

    fun getAlerts(): Flowable<List<Alert>>

    fun getNumberAlerts(): Flowable<Int>

    fun getAlertMethods(): Single<AlertMethodsResponse>

    fun updateAlertMethods(param: UpdateAlertMethodsUseCase.Param): Single<ResponseStatus>

    fun logout(): Completable

    fun save(param: SaveKycInfoUseCase.Param): Completable

    fun save(param: SavePersonalInfoUseCase.Param): Single<KycResponseStatus>

    fun save(param: SaveIdPassportUseCase.Param): Single<KycResponseStatus>

    fun resizeImage(param: ResizeImageUseCase.Param): Single<String>

    fun decode(param: Base64DecodeUseCase.Param): Single<ByteArray>

    fun submit(param: SubmitUserInfoUseCase.Param): Single<KycResponseStatus>

    fun reSubmit(param: ReSubmitUserInfoUseCase.Param): Single<KycResponseStatus>

    fun updatePushNotification(param: UpdatePushTokenUseCase.Param): Single<ResponseStatus>

    fun saveLocal(param: SaveLocalPersonalInfoUseCase.Param): Completable

    fun getRating(): Single<RatingInfo>

    fun saveRating(param: SaveRatingInfoUseCase.Param): Completable

    fun getNotifications(): Single<List<Notification>>

    fun getUnReadNotifications(): Flowable<Int>

    fun readNotifications(param: ReadNotificationsUseCase.Param): Single<ResponseStatus>

    fun dataTransfer(param: DataTransferUseCase.Param): Single<DataTransferStatus>
}
