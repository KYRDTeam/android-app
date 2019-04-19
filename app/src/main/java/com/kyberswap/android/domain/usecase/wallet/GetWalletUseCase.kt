package com.kyberswap.android.domain.usecase.wallet

import android.support.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.PasswordRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val passwordRepository: PasswordRepository
) : SequentialUseCase<Wallet, String>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Wallet): Single<String> {
        return passwordRepository.getPassword(param)
    }
}
