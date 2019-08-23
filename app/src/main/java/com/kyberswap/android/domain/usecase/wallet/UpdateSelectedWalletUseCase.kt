package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class UpdateSelectedWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository,
    private val balanceRepository: BalanceRepository
) : SequentialUseCase<UpdateSelectedWalletUseCase.Param, Pair<Wallet, List<Token>>>(
    schedulerProvider
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<Pair<Wallet, List<Token>>> {
        return walletRepository.updatedSelectedWallet(param).flatMap { wallet ->
            balanceRepository.getChange24h().first(listOf()).map { tokens ->
                Pair(wallet, tokens)
            }
        }
    }

    class Param(val wallet: Wallet)
}
