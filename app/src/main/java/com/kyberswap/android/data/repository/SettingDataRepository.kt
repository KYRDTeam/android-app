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
import java.security.SecureRandom
import javax.inject.Inject


class SettingDataRepository @Inject constructor(
    private val passCodeDao: PassCodeDao

) : SettingRepository {
    override fun getPin(): Single<String?> {
        return Single.fromCallable {
            passCodeDao.findPassCode()?.digest


    }


    override fun verifyPin(param: VerifyPinUseCase.Param): Single<VerifyStatus> {
        return Single.fromCallable {
            val newPin = hasPassCode(param.pin)
            val currentPin = passCodeDao.findPassCode()
            if (newPin == currentPin?.digest) {
                VerifyStatus(true)
     else {
                VerifyStatus(false)
    

    }

    override fun savePin(param: SavePinUseCase.Param): Completable {
        return Completable.fromCallable {
            passCodeDao.createNewPassCode(PassCode(digest = hasPassCode(param.pin)))

    }

    private fun hasPassCode(passCode: String): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        return Numeric.toHexStringNoPrefix(md.digest(passCode.toByteArray(StandardCharsets.UTF_8)))

    }


}