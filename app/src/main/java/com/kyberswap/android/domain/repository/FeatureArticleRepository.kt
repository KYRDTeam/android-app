package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Article
import io.reactivex.Single

interface FeatureArticleRepository {

    fun featureArticles(articleFeatureId: Long, page: Int): Single<List<Article>>
}