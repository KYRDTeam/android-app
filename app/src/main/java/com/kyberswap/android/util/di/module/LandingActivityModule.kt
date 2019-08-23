package com.kyberswap.android.util.di.module

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.landing.LandingActivityViewModel
import com.kyberswap.android.presentation.landing.LandingFragment
import com.kyberswap.android.presentation.landing.LandingViewModel
import com.kyberswap.android.presentation.main.kybercode.KyberCodeFragment
import com.kyberswap.android.presentation.main.kybercode.KyberCodeViewModel
import com.kyberswap.android.util.di.ViewModelKey
import com.kyberswap.android.util.di.scope.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface LandingActivityModule {

    @Binds
    fun bindsAppCompatActivity(landingActivity: LandingActivity): AppCompatActivity

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLandingFragment(): LandingFragment

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeKyberCodeFragment(): KyberCodeFragment

    @Binds
    @IntoMap
    @ViewModelKey(LandingViewModel::class)
    fun bindLandingViewModel(
        landingViewModel: LandingViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LandingActivityViewModel::class)
    fun bindLandingActivityViewModel(
        landingActivityViewModel: LandingActivityViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(KyberCodeViewModel::class)
    fun bindKyberCodeViewModel(
        kyberCodeViewModel: KyberCodeViewModel
    ): ViewModel
}