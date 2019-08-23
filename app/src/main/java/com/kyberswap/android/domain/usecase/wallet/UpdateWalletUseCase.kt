package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class UpdateWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : CompletableUseCase<Wallet, Any?>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Wallet): Completable {
        return walletRepository.updateWallet(param)
    }
}
