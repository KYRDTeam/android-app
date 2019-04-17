package com.kyberswap.android.util.di.module

import android.arch.lifecycle.ViewModelProvider
import com.kyberswap.android.util.di.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
interface ViewModelModule {
    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
