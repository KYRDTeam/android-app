package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.HomeApi
import com.kyberswap.android.data.mapper.ArticleMapper
import com.kyberswap.android.domain.model.Article
import com.kyberswap.android.domain.repository.FeatureArticleRepository
import io.reactivex.Single
import javax.inject.Inject

class FeatureArticleDataRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val mapper: ArticleMapper
) : FeatureArticleRepository {
    override fun featureArticles(articleFeatureId: Long, page: Int): Single<List<Article>> {
        return homeApi.getFeatureArticles(articleFeatureId, page)
            .map { it -> it.article }
            .toFlowable()
            .flatMapIterable { article -> article }
            .map {
                mapper.transform(it)
            }
            .toList()
    }
}