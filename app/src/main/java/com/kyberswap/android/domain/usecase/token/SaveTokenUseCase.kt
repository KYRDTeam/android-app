package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val tokenRepository: TokenRepository
) : CompletableUseCase<SaveTokenUseCase.Param, List<Token>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return tokenRepository.saveToken(param)
    }

    class Param(val token: Token)
}
