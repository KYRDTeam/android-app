package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.usecase.profile.*
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

    fun fetchUserInfo(): Flowable<UserInfo>

    fun getAlerts(): Flowable<List<Alert>>

    fun logout(): Completable

    fun save(param: SaveKycInfoUseCase.Param): Completable

    fun save(param: SavePersonalInfoUseCase.Param): Single<KycResponseStatus>

    fun save(param: SaveIdPassportUseCase.Param): Single<KycResponseStatus>

    fun resizeImage(param: ResizeImageUseCase.Param): Single<String>

    fun decode(param: Base64DecodeUseCase.Param): Single<ByteArray>

    fun submit(param: SubmitUserInfoUseCase.Param): Single<KycResponseStatus>

    fun reSubmit(param: ReSubmitUserInfoUseCase.Param): Single<KycResponseStatus>
}
