package com.kyberswap.android.data.repository

import com.kyberswap.android.data.db.PassCodeDao
import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.repository.SettingRepository
import com.kyberswap.android.domain.usecase.setting.SavePinUseCase
import com.kyberswap.android.domain.usecase.setting.VerifyPinUseCase
import io.reactivex.Completable
import io.reactivex.Single
import org.web3j.utils.Numeric
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject


class SettingDataRepository @Inject constructor(
    private val passCodeDao: PassCodeDao

) : SettingRepository {

    override fun getPin(): Single<PassCode> {
        return Single.fromCallable {
            passCodeDao.findPassCode()
        }
    }

    override fun verifyPin(param: VerifyPinUseCase.Param): Single<VerifyStatus> {
        return Single.fromCallable {
            val newPin = hash(param.pin)
            val passCode = passCodeDao.findPassCode()
            if (newPin == passCode?.digest) {
                passCode.let {
                    passCodeDao.updatePassCode(
                        passCode.copy(
                            remainNum = 0,
                            time = 0
                        )
                    )
                }
                VerifyStatus(true)
            } else {
                passCode?.let {
                    passCodeDao.updatePassCode(
                        passCode.copy(
                            remainNum = param.remainNum,
                            time = param.time
                        )
                    )
                }

                VerifyStatus(false)
            }
        }
    }

    override fun savePin(param: SavePinUseCase.Param): Completable {
        return Completable.fromCallable {
            passCodeDao.createNewPassCode(PassCode(digest = hash(param.pin)))
        }
    }

    private fun hash(passCode: String): String {
        val salt = ByteArray(16)
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        return Numeric.toHexStringNoPrefix(md.digest(passCode.toByteArray(StandardCharsets.UTF_8)))

    }
}