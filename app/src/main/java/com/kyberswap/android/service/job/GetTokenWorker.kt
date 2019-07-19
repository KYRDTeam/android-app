package com.kyberswap.android.service.job

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.util.TokenClient
import com.kyberswap.android.util.di.AppComponent
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetTokenWorker constructor(params: Int) : Job(
    Params(params)
        .requireNetwork()
        .persist()
) {

    @Inject
    @Transient
    lateinit var tokenApi: TokenApi

    @Inject
    @Transient
    lateinit var tokenClient: TokenClient

    override fun onAdded() {


    }

    fun inject(appComponent: AppComponent) {
        appComponent.inject(this)
    }


    @Throws(Throwable::class)
    override fun onRun() {

        tokenApi.getChange24h().toObservable()
            .repeatWhen {
                it.delay(5, TimeUnit.SECONDS)
    .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
    .doOnNext { Timber.e("onNext") }

    }

    private fun <T> zipWithFlatMap(): ObservableTransformer<T, Long> {
        return ObservableTransformer { observable ->
            observable.zipWith(
                Observable.range(COUNTER_START, ATTEMPTS),
                BiFunction<T, Int, Int> { _: T, u: Int -> u })
                .flatMap { t -> Observable.timer(t * 5L, TimeUnit.SECONDS) }

    }

    companion object {
        private val COUNTER_START = 1
        private val ATTEMPTS = 5
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {

    }

    override fun shouldReRunOnThrowable(
        throwable: Throwable,
        runCount: Int,
        maxRunCount: Int
    ): RetryConstraint? {
        return null
    }
}
