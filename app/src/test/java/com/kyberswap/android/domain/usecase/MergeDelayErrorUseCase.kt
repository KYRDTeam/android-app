package com.kyberswap.android.domain.usecase

import com.kyberswap.android.domain.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer

abstract class MergeDelayErrorUseCase<in PARAM, RESPONSE> protected constructor(
    private val schedulerProvider: SchedulerProvider
) {
    private val compositeDisposable = CompositeDisposable()

    fun execute(onSuccess: Consumer<RESPONSE>, onError: Consumer<Throwable>, param: PARAM) {
        val disposable = buildUseCaseFlowable(param)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui(), true)
            .subscribe(onSuccess, onError)
        compositeDisposable.add(disposable)
    }

    protected abstract fun buildUseCaseFlowable(param: PARAM): Flowable<RESPONSE>

    fun dispose() {
        compositeDisposable.clear()
    }
}