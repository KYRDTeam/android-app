package com.kyberswap.android.domain.usecase.walletconnect

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import io.reactivex.Completable
import javax.inject.Inject

class WalletConnectSendTransactionUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : CompletableUseCase<WalletConnectSendTransactionUseCase.Param, String>(schedulerProvider) {

    class Param(
        val id: Long,
        val transaction: WCEthereumTransaction,
        val wallet: Wallet
    )


    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return walletRepository.sendTransaction(param)
    }
}
