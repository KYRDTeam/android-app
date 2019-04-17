package com.kyberswap.android.util.di.module

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.landing.LandingViewModel
import com.kyberswap.android.presentation.splash.SplashActivity
import com.kyberswap.android.presentation.splash.SplashViewModel
import com.kyberswap.android.util.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface LandingActivityModule {

    @Binds
    fun bindsAppCompatActivity(landingActivity: LandingActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(LandingViewModel::class)
    fun bindLandingViewModel(
            landingViewModel: LandingViewModel
    ): ViewModel
}