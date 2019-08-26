package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class PreloadUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val balanceRepository: BalanceRepository,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository
) : FlowableUseCase<String?, Wallet>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<Wallet> {
        return balanceRepository.getBalance().toFlowable().flatMap {
            walletRepository.getSelectedWallet()

    }
}
