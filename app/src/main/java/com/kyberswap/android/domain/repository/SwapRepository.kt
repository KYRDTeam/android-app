package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Cap
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.usecase.swap.GetCapUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface SwapRepository {

    fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap>

    fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable

    fun saveSwap(param: SaveSwapUseCase.Param): Completable

    fun getGasPrice(): Single<Gas>

    fun getCap(param: GetCapUseCase.Param): Single<Cap>
}