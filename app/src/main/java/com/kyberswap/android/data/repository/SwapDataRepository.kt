package com.kyberswap.android.data.repository

import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.wallet.GetSwapDataUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapDataTokenUseCase
import com.kyberswap.android.domain.usecase.wallet.SaveSwapUseCase
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject


class SwapDataRepository @Inject constructor(
    private val swapDao: SwapDao,
    private val tokenDao: TokenDao
) : SwapRepository {
    override fun saveSwap(param: SaveSwapUseCase.Param): Completable {
        return Completable.fromCallable {
            swapDao.insertSwap(param.swap)

    }

    override fun saveSwapData(param: SaveSwapDataTokenUseCase.Param): Completable {
        return Completable.fromCallable {
            val currentSwapForWalletAddress =
                swapDao.findSwapDataByAddress(param.walletAddress).blockingFirst()
            val tokenBySymbol = tokenDao.getTokenBySymbol(param.token.tokenSymbol)
            if (param.isSourceToken) {
                currentSwapForWalletAddress.tokenSource = tokenBySymbol ?: Token()
     else {
                currentSwapForWalletAddress.tokenDest = tokenBySymbol ?: Token()
    
            swapDao.updateSwap(currentSwapForWalletAddress)

    }

    override fun getSwapData(param: GetSwapDataUseCase.Param): Flowable<Swap> {
        var defaultSwap = Swap()
        if (swapDao.all.blockingFirst().isNullOrEmpty()) {
            val defaultSourceToken = tokenDao.getTokenBySymbol(Token.ETH)
            val defaultDestToken = tokenDao.getTokenBySymbol(Token.KNC)
            defaultSwap = Swap(
                param.walletAddress,
                defaultSourceToken ?: Token(),
                defaultDestToken ?: Token(),
                sourceAmount = "",
                destAmount = "",
                expectedRate = "",
                slippageRate = ""
            )
            swapDao.insertSwap(defaultSwap)

        return swapDao.findSwapDataByAddress(param.walletAddress).defaultIfEmpty(
            defaultSwap
        )
    }

}