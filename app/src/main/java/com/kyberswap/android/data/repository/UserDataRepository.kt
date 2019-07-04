package com.kyberswap.android.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.db.AlertDao
import com.kyberswap.android.data.db.UserDao
import com.kyberswap.android.data.mapper.UserMapper
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.profile.*
import com.kyberswap.android.presentation.main.profile.kyc.KycInfoType
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.ceil


class UserDataRepository @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val storageMediator: StorageMediator,
    private val userMapper: UserMapper,
    private val alertDao: AlertDao
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
            it.sortedByDescending { it.id }
        }

    }

    override fun userInfo(): Single<UserInfo?> {
        return Single.fromCallable {
            userDao.getUser() ?: UserInfo()
        }
    }

    override fun fetchUserInfo(): Flowable<UserInfo> {
        return Flowable.mergeDelayError(
            userDao.all,
            userApi.getUserInfo().map {
                userMapper.transform(it)
            }
                .doAfterSuccess {
                    userDao.updateUser(it)
                }.toFlowable()
        )
    }

    override fun getUserInfo(): Flowable<UserInfo> {
        return userDao.all
    }

    override fun getUser(): Flowable<UserInfo> {
        return userDao.all
    }

    override fun resetPassword(param: ResetPasswordUseCase.Param): Single<ResponseStatus> {
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

    override fun signUp(param: SignUpUseCase.Param): Single<ResponseStatus> {
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

    override fun save(param: SaveKycInfoUseCase.Param): Completable {
        return Completable.fromCallable {
            val user = userDao.getUser() ?: UserInfo()
            val currentKycInfo = user.kycInfo

            val kycInfo = when (param.kycInfoType) {
                KycInfoType.NATIONALITY -> currentKycInfo.copy(nationality = param.value)
                KycInfoType.COUNTRY_OF_RESIDENCE -> currentKycInfo.copy(residentialAddress = param.value)
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
            info.lastName,
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
            info.taxIdentificationNumber

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


    companion object {
        private const val MAX_IMAGE_SIZE = 1000 * 1024
    }
}