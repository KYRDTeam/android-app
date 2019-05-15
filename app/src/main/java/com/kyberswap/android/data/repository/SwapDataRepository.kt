package com.kyberswap.android.data.repository

import android.content.Context
import com.kyberswap.android.R
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.mapper.CapMapper
import com.kyberswap.android.data.mapper.GasMapper
import com.kyberswap.android.domain.model.Cap
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.swap.EstimateGasUseCase
import com.kyberswap.android.domain.usecase.swap.GetCapUseCase
import com.kyberswap.android.domain.usecase.wallet.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapUseCase
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.web3j.protocol.core.methods.response.EthEstimateGas
import javax.inject.Inject
import kotlin.math.pow


class SwapDataRepository @Inject constructor(
    private val context: Context,
    private val swapDao: SwapDao,
    private val tokenDao: TokenDao,
    private val api: SwapApi,
    private val mapper: GasMapper,
    private val capMapper: CapMapper,
    private val tokenClient: TokenClient
) : SwapRepository {

    override fun estimateGas(param: EstimateGasUseCase.Param): Single<EthEstimateGas> {
        return Single.fromCallable {

            param.swap.tokenSource.tokenDecimal
            tokenClient.estimateGas(
                param.wallet.address,
                context.getString(R.string.kyber_address),
                param.swap.tokenSource.tokenAddress,
                param.swap.tokenDest.tokenAddress,
                param.swap.sourceAmount.toBigDecimalOrDefaultZero().times(
                    10.0.pow(param.swap.tokenSource.tokenDecimal)
                        .toBigDecimal()
                ).toPlainString(),
                param.swap.tokenSource.isETH()
            )
        }
    }

    override fun getCap(param: GetCapUseCase.Param): Single<Cap> {
        return api.getCap(param.walletAddress).map { capMapper.transform(it) }
    }

    override fun getGasPrice(): Single<Gas> {
        return api.getGasPrice().map { it.data }
            .map { mapper.transform(it) }
    }

    override fun saveSwap(param: SaveSwapUseCase.Param): Completable {
        return Completable.fromCallable {
            swapDao.insertSwap(param.swap)
        }
    }

    override fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val currentSwapForWalletAddress =
                swapDao.findSwapDataByAddress(param.walletAddress).blockingFirst()
            val tokenBySymbol = tokenDao.getTokenBySymbol(param.token.tokenSymbol)
            val token = if (param.isSourceToken) {
                currentSwapForWalletAddress.copy(tokenSource = tokenBySymbol ?: Token())
            } else {
                currentSwapForWalletAddress.copy(tokenDest = tokenBySymbol ?: Token())
            }
            swapDao.updateSwap(token)
        }
    }

    override fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap> {
        val swap = swapDao.findSwapByAddress(param.walletAddress)
        val defaultSwap = if (swap == null) {
            val defaultSourceToken = tokenDao.getTokenBySymbol(Token.ETH)
            val defaultDestToken = tokenDao.getTokenBySymbol(Token.KNC)
            Swap(
                param.walletAddress,
                defaultSourceToken ?: Token(),
                defaultDestToken ?: Token(),
                sourceAmount = "",
                destAmount = "",
                expectedRate = "",
                slippageRate = ""
            )

        } else {
            val tokenSource = tokenDao.getTokenBySymbol(swap.tokenSource.tokenSymbol) ?: Token()
            val tokenDest = tokenDao.getTokenBySymbol(swap.tokenDest.tokenSymbol) ?: Token()
            swap.copy(tokenSource = tokenSource, tokenDest = tokenDest)
        }
        swapDao.insertSwap(defaultSwap)
        return swapDao.findSwapDataByAddress(param.walletAddress).defaultIfEmpty(
            defaultSwap
        )
    }

}