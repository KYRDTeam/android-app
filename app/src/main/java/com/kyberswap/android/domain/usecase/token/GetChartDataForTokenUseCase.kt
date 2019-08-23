package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Chart
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import com.kyberswap.android.presentation.main.balance.chart.ChartType
import io.reactivex.Single
import javax.inject.Inject

class GetChartDataForTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val tokenRepository: TokenRepository
) : SequentialUseCase<GetChartDataForTokenUseCase.Param, Chart>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Chart> {
        return tokenRepository.getChartData(param)
    }

    class Param(val token: Token, val charType: ChartType, val rateType: String = "mid")


}
