package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.*
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendUseCase
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.web3j.protocol.core.methods.response.EthEstimateGas

interface SwapRepository {

    fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap>

    fun getSendData(param: GetSendTokenUseCase.Param): Flowable<Send>

    fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable

    fun saveSwap(param: SaveSwapUseCase.Param): Completable

    fun saveSend(param: SaveSendTokenUseCase.Param): Completable

    fun saveSend(param: SaveSendUseCase.Param): Completable

    fun getGasPrice(): Flowable<Gas>

    fun getCap(param: GetCapUseCase.Param): Single<Cap>

    fun estimateAmount(param: EstimateAmountUseCase.Param): Single<EstimateAmount>

    fun estimateGas(param: EstimateGasUseCase.Param): Single<EthEstimateGas>

    fun swapToken(param: SwapTokenUseCase.Param): Single<String>

    fun transferToken(param: TransferTokenUseCase.Param): Single<String>

    fun estimateGas(param: EstimateTransferGasUseCase.Param): Single<EthEstimateGas>
}