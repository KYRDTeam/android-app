package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetSelectedWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : FlowableUseCase<String?, Wallet>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<Wallet> {
        return walletRepository.getSelectedWallet()
    }
}
