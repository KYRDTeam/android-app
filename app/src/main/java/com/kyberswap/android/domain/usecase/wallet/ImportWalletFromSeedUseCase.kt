package com.kyberswap.android.domain.usecase.wallet

import android.support.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Wallet
import javax.inject.Inject

class ImportWalletFromSeedUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<ImportWalletFromSeedUseCase.Param, Wallet>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<Wallet> {
        return walletRepository.importWallet(param)

    }

    class Param(val seed: String, val walletName: String)
}
