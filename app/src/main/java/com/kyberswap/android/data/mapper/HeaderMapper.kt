package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity
import com.kyberswap.android.domain.model.Header
import javax.inject.Inject

class HeaderMapper @Inject constructor() {

    fun transform(entities: List<ArticleFeatureEntity>): List<Header> {
        val headers: MutableList<Header> = mutableListOf()
        for (entity in entities) {
            headers.add(Header(entity))
        }
        return headers
    }
}