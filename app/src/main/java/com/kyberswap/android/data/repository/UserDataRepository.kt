package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.db.UserDao
import com.kyberswap.android.data.mapper.UserMapper
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.UserStatus
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.profile.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.profile.LoginUseCase
import com.kyberswap.android.domain.usecase.profile.ResetPasswordUseCase
import com.kyberswap.android.domain.usecase.profile.SignUpUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class UserDataRepository @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val storageMediator: StorageMediator,
    private val userMapper: UserMapper
) : UserRepository {

    override fun logout(): Completable {
        return Completable.fromCallable {
            storageMediator.clearToken()
            userDao.deleteAllUsers()
        }

    }

    override fun getAlerts(): Single<List<Alert>> {
        return userApi.getAlert().map {
            userMapper.transform(it.alerts)
        }
    }

    override fun userInfo(): Single<UserInfo?> {
        return Single.fromCallable {
            userDao.getUser() ?: UserInfo()
        }

    }

    override fun getUser(): Flowable<UserInfo> {
        return userDao.all
    }

    override fun resetPassword(param: ResetPasswordUseCase.Param): Single<UserStatus> {
        return userApi.resetPassword(param.email).map {
            userMapper.transform(it)
        }
    }

    override fun loginSocial(param: LoginSocialUseCase.Param): Single<LoginUser> {
        return userApi.socialLogin(
            param.socialInfo.type.value,
            param.socialInfo.accessToken,
            param.socialInfo.subscription,
            param.socialInfo.photoUrl,
            param.socialInfo.twoFa,
            param.socialInfo.displayName,
            param.socialInfo.oAuthToken,
            param.socialInfo.oAuthTokenSecret,
            param.confirmSignUp
        )
            .map { userMapper.transform(it) }
            .doAfterSuccess {
                userDao.updateUser(it.userInfo)
                storageMediator.applyToken(it.authInfo)
            }
    }

    override fun login(param: LoginUseCase.Param): Single<LoginUser> {
        return userApi.login(param.email, param.password)
            .map { userMapper.transform(it) }
            .doAfterSuccess {
                userDao.updateUser(it.userInfo)
                storageMediator.applyToken(it.authInfo)
            }
    }

    override fun signUp(param: SignUpUseCase.Param): Single<UserStatus> {
        return userApi.register(
            param.email,
            param.password,
            param.password,
            param.displayName,
            param.isSubscription
        ).map {
            userMapper.transform(it)
        }
    }


}