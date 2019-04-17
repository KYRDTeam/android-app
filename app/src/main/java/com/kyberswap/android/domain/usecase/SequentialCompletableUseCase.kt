package com.kyberswap.android.domain.usecase

import com.kyberswap.android.domain.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer

abstract class SequentialCompletableUseCase<in PARAM> protected constructor(
    private val schedulerProvider: SchedulerProvider
) {
    private val compositeDisposable = CompositeDisposable()

    fun execute(onSuccess: Action, onError: Consumer<Throwable>, param: PARAM) {
        val disposable = buildUseCaseCompletable(param)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(onSuccess, onError)
        compositeDisposable.add(disposable)
    }

    protected abstract fun buildUseCaseCompletable(param: PARAM): Completable

    fun dispose() {
        compositeDisposable.clear()
    }
}