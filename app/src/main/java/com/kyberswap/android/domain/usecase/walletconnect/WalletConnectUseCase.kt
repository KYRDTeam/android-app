package com.kyberswap.android.domain.usecase.walletconnect

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import io.reactivex.Completable
import javax.inject.Inject

class WalletConnectUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : CompletableUseCase<WalletConnectUseCase.Param, String>(schedulerProvider) {

    class Param(
        val sessionInfo: String,
        val walletAddress: String,
        val onSessionRequest: (id: Long, peer: WCPeerMeta) -> Unit,
        val onEthSendTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit,
        val onEthSign: (id: Long, message: WCEthereumSignMessage) -> Unit,
        val onDisconnect: (code: Int, reason: String) -> Unit,
        val onFailure: (Throwable) -> Unit
    )


    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return walletRepository.walletConnect(param)
    }
}
