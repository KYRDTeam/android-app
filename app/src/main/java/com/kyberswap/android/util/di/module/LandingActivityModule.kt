package com.kyberswap.android.util.di.module

import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.presentation.landing.LandingActivity
import com.kyberswap.android.presentation.landing.LandingFragment
import com.kyberswap.android.util.di.scope.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface LandingActivityModule {

    @Binds
    fun bindsAppCompatActivity(landingActivity: LandingActivity): AppCompatActivity

    @FragmentScoped
    @ContributesAndroidInjector
    fun contributeLandingFragment(): LandingFragment

}