package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.splash.SplashActivity
import com.kyberswap.android.presentation.splash.SplashViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SplashActivityModule {

    @Binds
    fun bindsAppCompatActivity(splashActivity: SplashActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    fun bindSplashViewModel(
        splashViewModel: SplashViewModel
    ): ViewModel
}