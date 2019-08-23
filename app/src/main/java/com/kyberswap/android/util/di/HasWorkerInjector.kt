package com.kyberswap.android.util.di

import com.birbit.android.jobqueue.Job
import dagger.android.AndroidInjector

interface HasWorkerInjector {
    fun workerInjector(): AndroidInjector<Job>
}