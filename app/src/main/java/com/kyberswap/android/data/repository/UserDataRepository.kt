package com.kyberswap.android.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.google.gson.Gson
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.api.notification.NotificationEntity
import com.kyberswap.android.data.db.AlertDao
import com.kyberswap.android.data.db.RatingDao
import com.kyberswap.android.data.db.UserDao
import com.kyberswap.android.data.mapper.UserMapper
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.AlertMethodsResponse
import com.kyberswap.android.domain.model.DataTransferStatus
import com.kyberswap.android.domain.model.KycResponseStatus
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.NotificationRequestBody
import com.kyberswap.android.domain.model.RatingInfo
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.SubscriptionNotification
import com.kyberswap.android.domain.model.UpdateSubscribedNotificationRequestBody
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.alert.UpdateAlertMethodsUseCase
import com.kyberswap.android.domain.usecase.notification.ReadNotificationsUseCase
import com.kyberswap.android.domain.usecase.notification.UpdateSubscribedTokenNotificationUseCase
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
import com.kyberswap.android.presentation.main.profile.kyc.KycInfoType
import com.kyberswap.android.util.rx.operator.zipWithFlatMap
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.Flowables
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil

class UserDataRepository @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val storageMediator: StorageMediator,
    private val userMapper: UserMapper,
    private val alertDao: AlertDao,
    private val ratingDao: RatingDao,
    private val context: Context
) : UserRepository {

    override fun logout(): Completable {
        return Completable.fromCallable {
            storageMediator.clearToken()
            userDao.deleteAllUsers()
            alertDao.deleteAllAlerts()
        }
    }

    override fun getAlerts(): Flowable<List<Alert>> {
        return Flowable.mergeDelayError(
            alertDao.all.map { alerts ->
                alerts.filter { it.isNotLocal }
            },
            userApi.getAlert().map {
                    userMapper.transform(it.alerts)
                }
                .doAfterSuccess {
                    alertDao.updateAlerts(it)
                }.toFlowable()
        ).map {
            it.sortedByDescending { it.time }
        }
    }

    override fun getNumberAlerts(): Flowable<Int> {
        return Flowable.mergeDelayError(
            alertDao.all.map { alerts ->
                alerts.filter { it.isNotLocal }.size
            },
            userApi.getAlert().map {
                userMapper.transform(it.alerts)
            }.toFlowable().map {
                it.size
            }
        )
    }

    override fun userInfo(): Single<UserInfo?> {
        return Single.fromCallable {
            userDao.getUser() ?: UserInfo()
        }
    }

    override fun pollingUserInfo(): Flowable<UserInfo> {
        return userApi.getUserInfo().map {
                userMapper.transform(it)
            }
            .doAfterSuccess {
                if (!it.success && it.message.equals(
                        context.getString(R.string.not_authenticated_message), true
                    )
                ) {
                    userDao.deleteAllUsers()
                } else {
                    val currentUser = userDao.getUser() ?: UserInfo()
                    userDao.updateUser(
                        it.copy(
                            kycInfo = currentUser.kycInfo
                        )
                    )
                }
            }
            .repeatWhen {
                it.delay(60, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun refreshUserInfo(): Single<UserInfo> {
        return userApi.getUserInfo().map {
            userMapper.transform(it)
        }.doAfterSuccess {
            userDao.updateUser(it)
        }
    }

    override fun fetchUserInfo(): Flowable<UserInfo> {
        return Flowable.mergeDelayError(
            userDao.all,
            Flowables.zip(
                Flowable.fromCallable {
                    userDao.getUser() != null
                }.flatMap {
                    if (it) {
                        userDao.all
                    } else {
                        Flowable.fromCallable {
                            UserInfo()
                        }
                    }
                }
                ,
                userApi.getUserInfo().map {
                    userMapper.transform(it)
                }.toFlowable()
            ) { local, remote ->
                val remoteInfo = remote.kycInfo
                val kycInfo = local.kycInfo.copy(
                    photoSelfie = remoteInfo.photoSelfie,
                    photoIdentityBackSide = remoteInfo.photoIdentityBackSide,
                    photoIdentityFrontSide = remoteInfo.photoIdentityFrontSide,
                    photoProofAddress = remoteInfo.photoProofAddress
                )
                local.copy(kycInfo = kycInfo, isLoaded = true)

            }
        )
    }

    override fun getUserInfo(): Flowable<UserInfo> {
        return userDao.all.defaultIfEmpty(UserInfo())
    }

    override fun getUser(): Flowable<UserInfo> {
        return userDao.all
    }

    override fun resetPassword(param: ResetPasswordUseCase.Param): Single<ResponseStatus> {
        return userApi.resetPassword(param.email.toLowerCase(Locale.getDefault())).map {
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
                param.confirmSignUp,
                param.socialInfo.twoFa
            )
            .map { userMapper.transform(it) }
            .doAfterSuccess {
                userDao.updateUser(it.userInfo)
                storageMediator.applyToken(it.authInfo)
            }
    }

    override fun login(param: LoginUseCase.Param): Single<LoginUser> {
        return userApi.login(
                param.email.toLowerCase(Locale.getDefault()),
                param.password,
                param.twoFa
            )
            .map { userMapper.transform(it) }
            .doAfterSuccess {
                userDao.updateUser(it.userInfo)
                storageMediator.applyToken(it.authInfo)
            }
    }

    override fun signUp(param: SignUpUseCase.Param): Single<ResponseStatus> {
        return userApi.register(
            param.email.toLowerCase(Locale.getDefault()),
            param.password,
            param.password,
            param.displayName,
            param.isSubscription
        ).map {
            userMapper.transform(it)
        }
    }

    override fun save(param: SaveKycInfoUseCase.Param): Completable {
        return Completable.fromCallable {
            val user = userDao.getUser() ?: UserInfo()
            val currentKycInfo = user.kycInfo

            val kycInfo = when (param.kycInfoType) {
                KycInfoType.NATIONALITY -> currentKycInfo.copy(nationality = param.value)
                KycInfoType.COUNTRY_OF_RESIDENCE -> currentKycInfo.copy(country = param.value)
                KycInfoType.PROOF_ADDRESS -> currentKycInfo.copy(documentProofAddress = param.value)
                KycInfoType.SOURCE_FUND -> currentKycInfo.copy(sourceFund = param.value)
                KycInfoType.OCCUPATION_CODE -> currentKycInfo.copy(occupationCode = param.value)
                KycInfoType.INDUSTRY_CODE -> currentKycInfo.copy(industryCode = param.value)
                KycInfoType.TAX_RESIDENCY_COUNTRY -> currentKycInfo.copy(taxResidencyCountry = param.value)
            }
            user.kycInfo = kycInfo
            userDao.updateUser(user)
        }
    }

    override fun save(param: SavePersonalInfoUseCase.Param): Single<KycResponseStatus> {
        val info = param.kycInfo
        return userApi.savePersonalInfo(
            info.firstName,
            info.middleName,
            info.lastName,
            info.nativeFullName,
            info.nationality,
            info.country,
            info.dob,
            if (info.gender) 1 else 0,
            info.residentialAddress,
            info.city,
            info.zipCode,
            info.documentProofAddress,
            info.photoProofAddress,
            info.occupationCode,
            info.industryCode,
            info.taxResidencyCountry,
            if (info.haveTaxIdentification == null) null else if (info.haveTaxIdentification == true) 1 else 0,
            info.taxIdentificationNumber,
            info.sourceFund
        ).map {
            userMapper.transform(it)
        }.doAfterSuccess {
            val user = userDao.getUser() ?: UserInfo()
            user.kycInfo = info
            userDao.updateUser(user)
        }
    }

    override fun resizeImage(param: ResizeImageUseCase.Param): Single<String> {
        return Single.fromCallable {
            val options = BitmapFactory.Options()
            val bmp = BitmapFactory.decodeFile(param.path, options)
            val bm = modifyOrientation(bmp, param.path)
            var streamLength = MAX_IMAGE_SIZE
            var compressQuality = 105
            val bmpStream = ByteArrayOutputStream()
            while (streamLength >= MAX_IMAGE_SIZE && compressQuality > 5) {
                try {
                    bmpStream.flush()
                    bmpStream.reset()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                compressQuality -= 5
                bm.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()
                streamLength = (ceil(bmpPicByteArray.size / 3.0) * 4).toInt()
            }


            android.util.Base64.encodeToString(
                bmpStream.toByteArray(),
                android.util.Base64.DEFAULT
            )
        }
    }

    override fun decode(param: Base64DecodeUseCase.Param): Single<ByteArray> {
        return Single.fromCallable {
            android.util.Base64.decode(
                param.image,
                android.util.Base64.DEFAULT
            )
        }
    }

    override fun save(param: SaveIdPassportUseCase.Param): Single<KycResponseStatus> {
        val info = param.kycInfo
        return userApi.saveIdentityInfo(
            info.documentId,
            info.documentType,
            info.documentIssueDate,
            info.issueDateNonApplicable,
            info.documentExpiryDate,
            info.expiryDateNonApplicable,
            info.photoSelfie,
            info.photoIdentityFrontSide,
            info.photoIdentityBackSide
        ).map {
            userMapper.transform(it)
        }.doAfterSuccess {
            val user = userDao.getUser() ?: UserInfo()
            user.kycInfo = info
            userDao.updateUser(user)
        }
    }

    override fun submit(param: SubmitUserInfoUseCase.Param): Single<KycResponseStatus> {
        return userApi.submit()
            .map {
                userMapper.transform(it)
            }
            .doAfterSuccess {
                val user = userDao.getUser() ?: UserInfo()
                userDao.updateUser(user.copy(kycStatus = UserInfo.PENDING))
            }
    }

    override fun reSubmit(param: ReSubmitUserInfoUseCase.Param): Single<KycResponseStatus> {
        return userApi.resubmit().map {
            userMapper.transform(it)
        }.doAfterSuccess {
            val user = userDao.getUser() ?: UserInfo()
            userDao.updateUser(user.copy(kycStatus = UserInfo.DRAFT))
        }
    }

    @Throws(IOException::class)
    fun modifyOrientation(bitmap: Bitmap, imageAbsolutePath: String): Bitmap {
        val ei = ExifInterface(imageAbsolutePath)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)

            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)

            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(
                bitmap,
                horizontal = true,
                vertical = false
            )

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(
                bitmap,
                horizontal = false,
                vertical = true
            )

            else -> bitmap
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun updatePushNotification(param: UpdatePushTokenUseCase.Param): Single<ResponseStatus> {
        return userApi.updatePushToken(param.userId).map {
            userMapper.transform(it)
        }
    }

    override fun getAlertMethods(): Single<AlertMethodsResponse> {
        return userApi.getAlertMethod().map {
            userMapper.transform(it)
        }
    }

    override fun updateAlertMethods(param: UpdateAlertMethodsUseCase.Param): Single<ResponseStatus> {
        val body =
            RequestBody.create(MediaType.parse("text/plain"), Gson().toJson(param.alertMethods))
        return userApi.updateAlertMethods(body).map {
            userMapper.transform(it)
        }
    }

    override fun saveLocal(param: SaveLocalPersonalInfoUseCase.Param): Completable {
        return Completable.fromCallable {
            val user = userDao.getUser() ?: UserInfo()
            user.kycInfo = param.kycInfo
            userDao.updateUser(user)
        }
    }

    override fun getRating(): Single<RatingInfo> {
        return Single.fromCallable {
            var rating = ratingDao.getRating()
            if (rating == null) {
                rating = RatingInfo(updatedAt = System.currentTimeMillis() / 1000L)
                ratingDao.insertRatingInfo(rating)
            }
            rating

        }
    }

    override fun saveRating(param: SaveRatingInfoUseCase.Param): Completable {
        return Completable.fromCallable {
            ratingDao.updateRatingInfo(param.ratingInfo)
        }
    }

    override fun getNotifications(): Single<List<Notification>> {
        return Flowable.range(1, Integer.MAX_VALUE)
            .concatMap {
                userApi.getNotifications(it, 50).toFlowable()
            }

            .takeUntil {
                it.pagingInfo.pageIndex ?: 0 > it.pagingInfo.pageCount ?: 0
            }.reduce(
                mutableListOf<NotificationEntity>(),
                { builder, response ->
                    if (response.pagingInfo.pageIndex == 1) {
                        builder.clear()
                    }
                    builder.addAll(response.data)
                    builder
                })
            .toFlowable()
            .flatMapIterable { tokenCurrency -> tokenCurrency }
            .map {
                userMapper.transform(it)
            }.toList()
    }

    override fun getSubscriptionNotifications(): Single<SubscriptionNotification> {
        return userApi.getSubscriptionNotifications().map {
            SubscriptionNotification(it)
        }.doAfterSuccess {
            val user = userDao.getUser()
            if (user != null && user.priceNoti != it.priceNoti) {
                userDao.updateUser(user.copy(priceNoti = it.priceNoti))
            }
        }
    }

    override fun togglePriceNoti(param: Boolean): Single<ResponseStatus> {
        return userApi.togglePriceNoti(param).doAfterSuccess {
            val user = userDao.getUser()?.copy(priceNoti = param)
            if (user != null) {
                userDao.updateUser(user)
            }
        }
    }

    override fun getUnReadNotifications(): Flowable<Int> {
        return userApi.getNotifications().map {
                it.pagingInfo.unreadCount ?: 0
            }.repeatWhen {
                it.delay(15, TimeUnit.SECONDS)
            }
            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
            }
    }

    override fun readNotifications(param: ReadNotificationsUseCase.Param): Single<ResponseStatus> {
        val body =
            RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(NotificationRequestBody(param.notifications.map {
                    it.id
                }))
            )
        return userApi.markAsRead(body).map {
            userMapper.transform(it)
        }
    }

    override fun updateSubscribedTokensNotification(param: UpdateSubscribedTokenNotificationUseCase.Param): Single<ResponseStatus> {
        val body =
            RequestBody.create(
                MediaType.parse("text/plain"),
                Gson().toJson(UpdateSubscribedNotificationRequestBody(param.notifications))
            )
        return userApi.updateSubscribedTokens(body).map {
            userMapper.transform(it)
        }
    }

    override fun dataTransfer(param: DataTransferUseCase.Param): Single<DataTransferStatus> {
        return userApi.transferAgreement(param.action).map { userMapper.transform(it) }
    }

    companion object {
        private const val MAX_IMAGE_SIZE = 800 * 1024
    }
}