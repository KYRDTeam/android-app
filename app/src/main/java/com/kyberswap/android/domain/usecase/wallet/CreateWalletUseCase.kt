package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import org.consenlabs.tokencore.wallet.Wallet
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : SequentialUseCase<CreateWalletUseCase.Param, Wallet>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<Wallet> {
        return walletRepository.createWallet(param)
//        return walletRepository.createWallet(param).flatMap { wallet ->
//            walletRepository.getMnemonic(GetMnemonicUseCase.Param(param.pinLock, wallet.id))
//
//        }
    }

    class Param(
        val pinLock: String
    )
}
