package com.kyberswap.android.util.di.module

import android.arch.lifecycle.ViewModel
import android.support.v7.app.AppCompatActivity
import com.kyberswap.android.presentation.home.HomeFragment
import com.kyberswap.android.presentation.home.HomeViewModel
import com.kyberswap.android.presentation.main.MainActivity
import com.kyberswap.android.presentation.main.MainViewModel
import com.kyberswap.android.util.di.scope.FragmentScoped
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface MainActivityModule {
    @Binds
    fun providesAppCompatActivity(mainActivity: MainActivity): AppCompatActivity

    @FragmentScoped
    @ContributesAndroidInjector(modules = [
        HomeFragmentModule::class
    ])
    fun contributeHomeFragment(): HomeFragment

    @Binds @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeViewModel(
        homeViewModel: HomeViewModel
    ): ViewModel

    @Binds @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindMainViewModel(
        mainViewModel: MainViewModel
    ): ViewModel
}
