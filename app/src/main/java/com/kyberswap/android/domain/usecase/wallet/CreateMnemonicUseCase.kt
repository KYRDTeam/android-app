package com.kyberswap.android.domain.usecase.wallet

import android.support.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.MnemonicRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class CreateMnemonicUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val mnemonicRepository: MnemonicRepository
) : SequentialUseCase<Int, List<String>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Int): Single<List<String>> {
        return mnemonicRepository.create12wordsAccount(param)

    }
}
