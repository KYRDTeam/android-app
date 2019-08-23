package com.kyberswap.android.util.di

import com.birbit.android.jobqueue.Job

object AndroidWorkerInjection {

    fun inject(worker: Job) {
        val application = worker.applicationContext
        if (application !is HasWorkerInjector) {
            throw RuntimeException(
                "${application.javaClass.canonicalName} does not implement ${HasWorkerInjector::class.java.canonicalName}"
            )
        }

        val workerInjector = (application as HasWorkerInjector).workerInjector()
        workerInjector.inject(worker)
    }
}