package com.kyberswap.android.domain.usecase

import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Article
import com.kyberswap.android.domain.repository.TopArticleRepository
import io.reactivex.Single
import javax.inject.Inject

class GetTopArticlesUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val topArticleRepository: TopArticleRepository
) : SequentialUseCase<Int, List<Article>>(schedulerProvider) {

    public override fun buildUseCaseSingle(param: Int): Single<List<Article>> {
        return topArticleRepository.topArticles(param)
    }
}