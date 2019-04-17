package com.kyberswap.android.util.ext

import io.reactivex.Completable
import io.reactivex.Single

fun <T> T.toSingle(): Single<T> {
    return Single.fromCallable {
        return@fromCallable this
    }
}

fun <T> T.toCompletable(): Completable {
    return Completable.fromCallable {
        return@fromCallable this
    }
}