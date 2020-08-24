package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Cap
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.KyberEnabled
import com.kyberswap.android.domain.model.MaxGasPrice
import com.kyberswap.android.domain.model.PlatformFee
import com.kyberswap.android.domain.model.QuoteAmount
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.usecase.send.ENSResolveUseCase
import com.kyberswap.android.domain.usecase.send.ENSRevertResolveUseCase
import com.kyberswap.android.domain.usecase.send.GetSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendTokenUseCase
import com.kyberswap.android.domain.usecase.send.SaveSendUseCase
import com.kyberswap.android.domain.usecase.send.TransferTokenUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateAmountUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateGasUseCase
import com.kyberswap.android.domain.usecase.swap.EstimateTransferGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetCapUseCase
import com.kyberswap.android.domain.usecase.swap.GetCombinedCapUseCase
import com.kyberswap.android.domain.usecase.swap.GetPlatformFeeUseCase
import com.kyberswap.android.domain.usecase.swap.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.swap.ResetSwapDataUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.swap.SaveSwapUseCase
import com.kyberswap.android.domain.usecase.swap.SwapTokenUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.web3j.protocol.core.methods.response.EthEstimateGas
import java.math.BigDecimal

interface SwapRepository {

    fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap>

    fun resetSwapData(param: ResetSwapDataUseCase.Param): Completable

    fun getSendData(param: GetSendTokenUseCase.Param): Flowable<Send>

    fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable

    fun saveSwap(param: SaveSwapUseCase.Param): Completable

    fun saveSend(param: SaveSendTokenUseCase.Param): Completable

    fun saveSend(param: SaveSendUseCase.Param): Completable

    fun getGasPrice(): Flowable<Gas>

    fun getCap(param: GetCapUseCase.Param): Single<Cap>

    fun estimateAmount(param: EstimateAmountUseCase.Param): Single<QuoteAmount>

    fun estimateGas(param: EstimateGasUseCase.Param): Single<BigDecimal>

    fun swapToken(param: SwapTokenUseCase.Param): Single<ResponseStatus>

    fun transferToken(param: TransferTokenUseCase.Param): Single<ResponseStatus>

    fun estimateGas(param: EstimateTransferGasUseCase.Param): Single<EthEstimateGas>

    fun getCap(param: GetCombinedCapUseCase.Param): Single<Cap>

    fun ensResolve(param: ENSResolveUseCase.Param): Single<String>

    fun ensRevertResolve(param: ENSRevertResolveUseCase.Param): Single<String>

    fun getKyberNetworkStatus(): Single<KyberEnabled>

    fun getPlatformFee(param: GetPlatformFeeUseCase.Param): Single<PlatformFee>

    fun getMaxGasPrice(): Single<MaxGasPrice>

    fun getHint(
        srcAddress: String, destAddress: String, amount: String, isReserveRouting: Boolean
    ): Single<String?>
}