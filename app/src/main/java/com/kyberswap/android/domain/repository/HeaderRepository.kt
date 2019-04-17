package com.kyberswap.android.domain.repository

import com.kyberswap.android.data.api.home.entity.HeaderEntity
import com.kyberswap.android.domain.model.Header
import com.kyberswap.android.domain.model.HeaderInfo
import io.reactivex.Flowable

interface HeaderRepository {

    fun headers(): Flowable<List<Header>>

    fun save(headerEntity: HeaderEntity)

    fun getLikeAndReviewInfo(): Flowable<HeaderInfo>
}