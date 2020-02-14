package com.kyberswap.android.domain.usecase.transaction

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class SpeedUpOrCancelTransactionUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val transactionRepository: TransactionRepository
) : SequentialUseCase<SpeedUpOrCancelTransactionUseCase.Param, Boolean>(schedulerProvider) {

    class Param(val transaction: Transaction, val wallet: Wallet, val isCancel: Boolean = false)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Boolean> {
        return transactionRepository.speedUpOrCancel(param)
    }
}
