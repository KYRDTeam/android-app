package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Article
import io.reactivex.Single

interface TopArticleRepository {

    fun topArticles(page: Int): Single<List<Article>>
}