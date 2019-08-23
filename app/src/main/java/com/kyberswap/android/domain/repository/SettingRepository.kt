package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.usecase.setting.SavePinUseCase
import com.kyberswap.android.domain.usecase.setting.VerifyPinUseCase
import io.reactivex.Completable
import io.reactivex.Single

interface SettingRepository {
    fun savePin(param: SavePinUseCase.Param): Completable

    fun verifyPin(param: VerifyPinUseCase.Param): Single<VerifyStatus>

    fun getPin(): Single<PassCode>
}