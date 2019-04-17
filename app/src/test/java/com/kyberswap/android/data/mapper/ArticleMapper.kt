package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.home.entity.ArticleEntity
import com.kyberswap.android.domain.model.Article
import javax.inject.Inject

class ArticleMapper @Inject constructor() {

    fun transform(entity: ArticleEntity): Article {
        return Article(entity)
    }
}