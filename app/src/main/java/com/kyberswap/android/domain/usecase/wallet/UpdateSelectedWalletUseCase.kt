package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class UpdateSelectedWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<UpdateSelectedWalletUseCase.Param, Wallet>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<Wallet> {
        return walletRepository.updatedSelectedWallet(param)
    }

    class Param(val wallet: Wallet)
}
