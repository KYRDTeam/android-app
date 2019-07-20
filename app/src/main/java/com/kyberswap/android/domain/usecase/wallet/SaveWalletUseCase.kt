package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : CompletableUseCase<SaveWalletUseCase.Param, Pair<Wallet, List<Word>>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseCompletable(param: Param): Completable {
        return walletRepository.saveWallet(param)
    }

    class Param(
        val wallet: Wallet
    )
}
