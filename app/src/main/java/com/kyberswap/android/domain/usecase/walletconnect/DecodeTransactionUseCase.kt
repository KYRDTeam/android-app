package com.kyberswap.android.domain.usecase.walletconnect

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import io.reactivex.Single
import javax.inject.Inject

class DecodeTransactionUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<DecodeTransactionUseCase.Param, Transaction>(schedulerProvider) {

    class Param(
        val message: WCEthereumTransaction,
        val wallet: Wallet
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Transaction> {
        return walletRepository.decodeTransaction(param)
    }
}
