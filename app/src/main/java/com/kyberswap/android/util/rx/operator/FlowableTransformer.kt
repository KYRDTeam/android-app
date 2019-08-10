package com.kyberswap.android.util.rx.operator

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

fun <T> zipWithFlatMap(): FlowableTransformer<T, Long> {
    return FlowableTransformer { flowable ->
        flowable.zipWith(
            Flowable.range(COUNTER_START, ATTEMPTS),
            BiFunction<T, Int, Int> { _: T, u: Int -> u })
            .flatMap { t -> Flowable.timer(t * 5L, TimeUnit.SECONDS) }
    }
}

private const val COUNTER_START = 1
private const val ATTEMPTS = 5