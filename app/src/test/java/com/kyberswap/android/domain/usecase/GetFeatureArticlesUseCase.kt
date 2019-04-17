package com.kyberswap.android.domain.usecase

import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Article
import com.kyberswap.android.domain.repository.FeatureArticleRepository
import io.reactivex.Single
import javax.inject.Inject

class GetFeatureArticlesUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val featureArticleRepository: FeatureArticleRepository
) : SequentialUseCase<GetFeatureArticlesUseCase.Param, List<Article>>(schedulerProvider) {

    public override fun buildUseCaseSingle(param: Param): Single<List<Article>> {
        return featureArticleRepository.featureArticles(param.featureArticleId, param.pageNumber)
    }

    class Param(val featureArticleId: Long, val pageNumber: Int)
}