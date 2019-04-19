package com.kyberswap.android.domain.usecase.wallet

import android.support.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<CreateWalletUseCase.Param, List<Word>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<List<Word>> {
        return walletRepository.createWallet(param).flatMap { wallet ->
            walletRepository.getMnemonic(GetMnemonicUseCase.Param(param.pinLock, wallet.id))

        }
    }

    class Param(
        val pinLock: String
    )

}