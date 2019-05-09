package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.usecase.wallet.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapDataTokenUseCase
import io.reactivex.Completable
import io.reactivex.Flowable

interface SwapRepository {

    fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap>

    fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable
}