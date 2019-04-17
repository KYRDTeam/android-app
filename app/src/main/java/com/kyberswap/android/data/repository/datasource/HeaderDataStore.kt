package com.kyberswap.android.data.repository.datasource

import com.kyberswap.android.data.api.home.entity.HeaderEntity
import io.reactivex.Flowable

interface HeaderDataStore {

    fun headers(): Flowable<HeaderEntity>

    fun save(headerEntity: HeaderEntity)
}