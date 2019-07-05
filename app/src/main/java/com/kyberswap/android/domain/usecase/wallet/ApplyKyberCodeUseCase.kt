package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class ApplyKyberCodeUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<ApplyKyberCodeUseCase.Param, Wallet>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<Wallet> {
        return walletRepository.createWallet(param)
    }

    class Param(
        val kyberCode: String,
        val walletName: String

    )
}