package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.data.api.chart.Data
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetToken24hVolUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val tokenRepository: TokenRepository
) : SequentialUseCase<GetToken24hVolUseCase.Param, Data>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Data> {
        return tokenRepository.get24hVol(param)
    }

    class Param(val token: Token)
}
