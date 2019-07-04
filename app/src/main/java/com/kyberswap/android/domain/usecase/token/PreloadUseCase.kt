package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import javax.inject.Inject

class PreloadUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val balanceRepository: BalanceRepository,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository
) : FlowableUseCase<String?, Pair<UserInfo?, Wallet>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<Pair<UserInfo?, Wallet>> {
        return balanceRepository.getBalance().toFlowable().flatMap {
            Flowables.zip(
                userRepository.userInfo().toFlowable(),
                walletRepository.getSelectedWallet()
            ) { user, wallet ->
                Pair(user, wallet)

    

    }
}
