package com.kyberswap.android.util.di;

import com.birbit.android.jobqueue.Job;

import java.util.Map;

import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.Multibinds;

@Module
public abstract class AndroidWorkerInjectionModule {

    @Multibinds
    abstract Map<Class<? extends Job>, AndroidInjector.Factory<? extends Job>>
    workerInjectorFactories();
}
